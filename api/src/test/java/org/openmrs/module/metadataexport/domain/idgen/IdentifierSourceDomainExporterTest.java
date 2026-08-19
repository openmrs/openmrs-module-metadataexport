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
import org.apache.commons.lang3.StringUtils;
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
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.initializer.api.CsvLine;
import org.openmrs.module.initializer.api.idgen.IdentifierSourceLineProcessor;
import org.openmrs.module.initializer.api.idgen.IdentifierSourceType;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentifierSourceDomainExporterTest {
	
	private final IdentifierSourceDomainExporter exporter = new IdentifierSourceDomainExporter();
	
	@Test
	void partitionsSourcesByTypeIncludingRetiredOnes() {
		SequentialIdentifierGenerator sequential = new SequentialIdentifierGenerator();
		SequentialIdentifierGenerator retiredSequential = new SequentialIdentifierGenerator();
		retiredSequential.setRetired(true);
		RemoteIdentifierSource remote = new RemoteIdentifierSource();
		remote.setUser("idgen-user");
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
	
	@Test
	void export_producesFilesInizCanClassifyAndRead(@TempDir File outDir) throws Exception {
		// CsvLine.getUuid() soft-validates the uuid format, so the fixtures need real ones
		SequentialIdentifierGenerator minimal = new SequentialIdentifierGenerator();
		minimal.setUuid("c1d8a345-3f10-11e4-adec-0800271c1b75");
		minimal.setName("Sequential");
		minimal.setFirstIdentifierBase("1000");
		minimal.setBaseCharacterSet("0123456789");
		SequentialIdentifierGenerator retired = new SequentialIdentifierGenerator();
		retired.setUuid("439559c2-a3a4-4a25-b4b2-1a0299e287ee");
		retired.setName("Retired");
		retired.setFirstIdentifierBase("1");
		retired.setBaseCharacterSet("0123456789");
		retired.setRetired(true);
		RemoteIdentifierSource remote = new RemoteIdentifierSource();
		remote.setUuid("9e1a2b3c-3f10-11e4-adec-0800271c1b75");
		remote.setName("Remote");
		remote.setUrl("https://idgen.example.org/generate");
		remote.setUser("idgen-user");
		IdentifierPool pool = new IdentifierPool();
		pool.setUuid("7e3f4d5a-3f10-11e4-adec-0800271c1b75");
		pool.setName("Pool");
		pool.setSource(minimal);
		IdentifierPool poolOfPool = new IdentifierPool();
		poolOfPool.setUuid("8f4a5b6c-3f10-11e4-adec-0800271c1b75");
		poolOfPool.setName("Pool of pool");
		poolOfPool.setSource(pool);
		
		exporter.export(Arrays.asList(minimal, retired, remote, pool, poolOfPool), new ExportContext(outDir));
		
		assertInizReads(outDir, IdentifierSourceDomainExporter.FILE_SEQUENTIAL, 2, 1000, IdentifierSourceType.SEQUENTIAL,
		    "base character set", "first identifier base");
		assertInizReads(outDir, IdentifierSourceDomainExporter.FILE_REMOTE, 1, 2000, IdentifierSourceType.REMOTE, "url",
		    "user", "password");
		assertInizReads(outDir, IdentifierSourceDomainExporter.FILE_POOL, 2, 3000, IdentifierSourceType.POOL,
		    "pool identifier source", "pool refill with task", "pool sequential allocation");
	}
	
	/**
	 * Reads an exported file back the way Iniz's parser does: blank cells become nulls, each row's
	 * source type is inferred by Iniz's own public getIdentifierSourceType, and every column the import
	 * requires must hold a value.
	 */
	private static void assertInizReads(File outDir, String fileName, int dataRows, int order, IdentifierSourceType type,
	        String... requiredHeaders) throws Exception {
		File csv = outDir.toPath().resolve(Paths.get("configuration", Domain.IDGEN.getName(), fileName)).toFile();
		try (CSVReader reader = new CSVReader(new FileReader(csv))) {
			List<String[]> rows = reader.readAll();
			assertEquals(dataRows + 1, rows.size(), fileName + " must hold every fixture source of its type");
			String[] header = rows.get(0);
			assertEquals(Integer.valueOf(order), BaseLineProcessor.getOrder(header));
			assertEquals("1", BaseLineProcessor.getVersion(header));
			for (String[] cells : rows.subList(1, rows.size())) {
				for (int i = 0; i < cells.length; i++) {
					if (StringUtils.isBlank(cells[i])) {
						cells[i] = null;
					}
				}
				CsvLine line = new CsvLine(header, cells);
				assertEquals(type, IdentifierSourceLineProcessor.getIdentifierSourceType(line),
				    fileName + " row " + line.getUuid() + " must be classifiable as its file's type");
				for (String required : requiredHeaders) {
					assertNotNull(line.get(required, true),
					    fileName + " row " + line.getUuid() + " must fill '" + required + "'");
				}
			}
		}
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
	void handlesOnlySourcesItWillActuallyExport() {
		assertTrue(exporter.handles(new SequentialIdentifierGenerator()));
		
		RemoteIdentifierSource remote = new RemoteIdentifierSource();
		remote.setUser("idgen-user");
		assertTrue(exporter.handles(remote));
		assertFalse(exporter.handles(new RemoteIdentifierSource()),
		    "a remote source without a user cannot be imported by Iniz");
		
		IdentifierPool pool = new IdentifierPool();
		pool.setSource(new SequentialIdentifierGenerator());
		assertTrue(exporter.handles(pool));
		assertFalse(exporter.handles(new IdentifierPool()), "a pool without a backing source cannot be imported by Iniz");
		IdentifierPool customBacked = new IdentifierPool();
		customBacked.setSource(new BaseIdentifierSource() {});
		assertFalse(exporter.handles(customBacked),
		    "handles() must agree with what export writes, or the manifest lists sources missing from the package");
		
		assertFalse(exporter.handles(new PatientIdentifierType()));
		assertFalse(exporter.handles(new BaseIdentifierSource() {}));
	}
	
	@Test
	void handlesFollowsThePoolBackingChain() {
		RemoteIdentifierSource remoteWithUser = new RemoteIdentifierSource();
		remoteWithUser.setUser("idgen-user");
		IdentifierPool remoteBacked = new IdentifierPool();
		remoteBacked.setSource(remoteWithUser);
		assertTrue(exporter.handles(remoteBacked));
		
		IdentifierPool userlessRemoteBacked = new IdentifierPool();
		userlessRemoteBacked.setSource(new RemoteIdentifierSource());
		assertFalse(exporter.handles(userlessRemoteBacked),
		    "a chain ending in an unimportable remote is unimportable itself");
		
		IdentifierPool selfBacked = new IdentifierPool();
		selfBacked.setSource(selfBacked);
		assertFalse(exporter.handles(selfBacked), "a pool cycle must terminate as unexportable, not hang");
		
		IdentifierPool a = new IdentifierPool();
		IdentifierPool b = new IdentifierPool();
		a.setSource(b);
		b.setSource(a);
		assertFalse(exporter.handles(a), "a two-pool cycle must terminate as unexportable, not hang");
	}
}
