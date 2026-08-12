/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.idgen;

import com.opencsv.CSVReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.OpenmrsObject;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.idgen.BaseIdentifierSource;
import org.openmrs.module.idgen.IdentifierPool;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.RemoteIdentifierSource;
import org.openmrs.module.idgen.SequentialIdentifierGenerator;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.ExportContext;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentifierSourceDomainExporterTest {
	
	private final IdentifierSourceDomainExporter exporter = new IdentifierSourceDomainExporter();
	
	@Test
	void partitionsSourcesByTypeIncludingRetiredOnes() {
		SequentialIdentifierGenerator sequential = new SequentialIdentifierGenerator();
		SequentialIdentifierGenerator retiredSequential = new SequentialIdentifierGenerator();
		retiredSequential.setRetired(true);
		RemoteIdentifierSource remote = new RemoteIdentifierSource();
		IdentifierPool pool = new IdentifierPool();
		pool.setSource(sequential);
		
		Map<String, Collection<IdentifierSource>> files = exporter
		        .partition(Arrays.asList(sequential, remote, pool, retiredSequential));
		
		assertEquals(3, files.size());
		assertTrue(files.get(IdentifierSourceDomainExporter.FILE_SEQUENTIAL).contains(sequential));
		assertTrue(files.get(IdentifierSourceDomainExporter.FILE_SEQUENTIAL).contains(retiredSequential),
		    "retired sources partition into their type file like live ones");
		assertTrue(files.get(IdentifierSourceDomainExporter.FILE_REMOTE).contains(remote));
		assertTrue(files.get(IdentifierSourceDomainExporter.FILE_POOL).contains(pool));
	}
	
	@Test
	void partitionOmitsFilesForAbsentTypes() {
		Map<String, Collection<IdentifierSource>> files = exporter
		        .partition(Arrays.asList(new SequentialIdentifierGenerator()));
		
		assertEquals(1, files.size(), "no empty per-type files — a type-column-less file would be unclassifiable");
		assertTrue(files.containsKey(IdentifierSourceDomainExporter.FILE_SEQUENTIAL));
	}
	
	@Test
	void partitionSkipsUnknownSourceSubclasses() {
		IdentifierSource custom = new BaseIdentifierSource() {};
		IdentifierPool pool = new IdentifierPool();
		pool.setSource(new SequentialIdentifierGenerator());
		
		Map<String, Collection<IdentifierSource>> files = exporter.partition(Arrays.asList(custom, pool));
		
		assertEquals(1, files.size(), "custom subclasses have no Iniz representation and land in no file");
		assertTrue(files.get(IdentifierSourceDomainExporter.FILE_POOL).contains(pool));
	}
	
	@Test
	void partitionSkipsPoolsWithoutAnImportableBackingSource() {
		IdentifierPool sourceless = new IdentifierPool();
		IdentifierPool customBacked = new IdentifierPool();
		customBacked.setSource(new BaseIdentifierSource() {});
		IdentifierPool good = new IdentifierPool();
		good.setSource(new SequentialIdentifierGenerator());
		
		Map<String, Collection<IdentifierSource>> files = exporter.partition(Arrays.asList(sourceless, customBacked, good));
		
		assertEquals(1, files.size());
		assertEquals(1, files.get(IdentifierSourceDomainExporter.FILE_POOL).size(),
		    "a pool row without a resolvable backing source uuid can never import");
		assertTrue(files.get(IdentifierSourceDomainExporter.FILE_POOL).contains(good));
	}
	
	@Test
	void poolFileIsOrderedAfterTheSourceFiles() {
		Integer sequential = exporter.order(IdentifierSourceDomainExporter.FILE_SEQUENTIAL);
		Integer remote = exporter.order(IdentifierSourceDomainExporter.FILE_REMOTE);
		Integer pool = exporter.order(IdentifierSourceDomainExporter.FILE_POOL);
		
		assertEquals(1000, sequential);
		assertEquals(2000, remote);
		assertEquals(3000, pool);
		assertTrue(pool > sequential && pool > remote, "pools reference backing sources, so they must load last");
	}
	
	@Test
	void export_writesOneFilePerTypeWithItsColumnsAndOrder(@TempDir File outDir) throws Exception {
		SequentialIdentifierGenerator sequential = new SequentialIdentifierGenerator();
		sequential.setUuid("seq-uuid");
		sequential.setName("Sequential");
		sequential.setFirstIdentifierBase("1000");
		sequential.setBaseCharacterSet("0123456789");
		RemoteIdentifierSource remote = new RemoteIdentifierSource();
		remote.setUuid("rem-uuid");
		remote.setName("Remote");
		remote.setUrl("https://idgen.example.org/generate");
		remote.setUser("idgen-user");
		IdentifierPool pool = new IdentifierPool();
		pool.setUuid("pool-uuid");
		pool.setName("Pool");
		pool.setSource(sequential);
		
		exporter.export(Arrays.asList(sequential, remote, pool), new ExportContext(outDir));
		
		assertEquals("1000", cell(outDir, IdentifierSourceDomainExporter.FILE_SEQUENTIAL, "first identifier base"),
		    "the sequential secondary exporter must be in the chain");
		assertEquals("https://idgen.example.org/generate", cell(outDir, IdentifierSourceDomainExporter.FILE_REMOTE, "url"),
		    "the remote secondary exporter must be in the chain");
		assertEquals("seq-uuid", cell(outDir, IdentifierSourceDomainExporter.FILE_POOL, "pool identifier source"),
		    "the pool secondary exporter must be in the chain");
		assertEquals("", cell(outDir, IdentifierSourceDomainExporter.FILE_POOL, "_order:3000"),
		    "each file carries its load-order header");
	}
	
	/** The single data row's value under the given header of an exported idgen CSV. */
	private static String cell(File outDir, String fileName, String header) throws Exception {
		File csv = outDir.toPath().resolve(Paths.get("configuration", Domain.IDGEN.getName(), fileName)).toFile();
		assertTrue(csv.exists(), "expected " + csv);
		try (CSVReader reader = new CSVReader(new FileReader(csv))) {
			List<String[]> rows = reader.readAll();
			assertEquals(2, rows.size(), fileName + " holds exactly its one source");
			int column = Arrays.asList(rows.get(0)).indexOf(header);
			assertTrue(column >= 0, fileName + " is missing header '" + header + "'");
			return rows.get(1)[column];
		}
	}
	
	@Test
	void dependenciesIncludeIdentifierType() {
		PatientIdentifierType type = new PatientIdentifierType();
		SequentialIdentifierGenerator source = new SequentialIdentifierGenerator();
		source.setIdentifierType(type);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(source);
		
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(type));
	}
	
	@Test
	void poolDependenciesIncludeBackingSource() {
		PatientIdentifierType type = new PatientIdentifierType();
		SequentialIdentifierGenerator backing = new SequentialIdentifierGenerator();
		IdentifierPool pool = new IdentifierPool();
		pool.setIdentifierType(type);
		pool.setSource(backing);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(pool);
		
		assertEquals(2, dependencies.size());
		assertTrue(dependencies.contains(type));
		assertTrue(dependencies.contains(backing));
	}
	
	@Test
	void dependenciesAreNullSafe() {
		assertTrue(exporter.getDependencies(new IdentifierPool()).isEmpty());
	}
	
	@Test
	void handlesOnlyTheSourceTypesInizCanRepresent() {
		assertTrue(exporter.handles(new SequentialIdentifierGenerator()));
		assertTrue(exporter.handles(new RemoteIdentifierSource()));
		assertTrue(exporter.handles(new IdentifierPool()));
		assertFalse(exporter.handles(new PatientIdentifierType()));
		assertFalse(exporter.handles(new BaseIdentifierSource() {}),
		    "handles() must agree with partition(), or selection puts sources in the manifest that export drops");
	}
}
