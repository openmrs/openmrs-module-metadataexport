Metadata Export
================

**Active development / experimental.** This module is an early proof of concept. APIs, output
format, and behaviour will change without notice, and not everything described here is fully
verified yet. Not for production use.

Description
-----------
Initializer ([openmrs-module-initializer](https://github.com/mekomsolutions/openmrs-module-initializer))
can *load* a `configuration/` content package into OpenMRS, but it cannot produce one. This module
does the reverse: it reads metadata out of a running, populated OpenMRS instance and writes it back
out in the Initializer format, so a configuration can be captured from a server and replayed
elsewhere.

It is export only. It never imports or applies metadata; loading remains Initializer's job.

Currently supported domains:

* Concepts (names, descriptions, class/datatype/version, numeric, complex, answers,
  mappings, attributes)
* Concept sources (name, description, HL7 code, unique ID)
* Encounter types (name, description, view/edit privileges)
* Privileges (name, description)
* Concept classes (name, description)
* Roles (name, description, privileges)
* Patient identifier types (name, description, required, format, format description, validator,
    location/uniqueness behavior)
* Visit types (name, description)
* Relationship types (name, description, a_is_to_b, b_is_to_a, preferred, weight)
* Attribute types (name, description, Min occurs, Max occurs, Datatype classname, Datatype config, Preferred handler classname, Handler config)
* Global properties (property, value) — written as XML, since Initializer loads this domain from
  XML rather than CSV
* Encounter Roles (name, description)
* Person Attribute Types (name, description, searchable, format, foreign uuid, edit privilege)
* Location Tags (name, description)
* Locations (name, description, parent location, tags, address fields) — parent locations and tags
  are pulled in via cross-domain closure
* Drugs (name, description, strength, concept drug, concept dosage form, ingredients, mappings) —
  drug/dosage-form/ingredient concepts are pulled in via cross-domain closure
* Order types (name, description, java class name, parent, concept classes) — parent order types and
  concept classes are pulled in via cross-domain closure
* Flags (name, criteria, evaluator, message, priority, enabled, tags, description)
* Order frequencies (frequency per day, concept frequency) — the referenced concept is pulled in via
  cross domain closure
* Programs (program concept, outcomes concept) — the referenced concepts are pulled in via
  cross domain closure
* Program workflows (program, workflow concept) — the referenced program and concept are pulled in
  via cross-domain closure
* Concept reference ranges (concept numeric, absolute/critical/normal low and high, criteria) — the
  referenced concept numeric is pulled in via cross domain closure
* Concept sets (parent concept, member concept, member type, sort weight) — the referenced parent
  and member concepts are pulled in via cross domain closure
* Program workflow states (workflow, state concept, initial, terminal) — the referenced workflow and
  state concept are pulled in via cross domain closure
* Procedure types (name, description) — requires the emrapi module (3.4+)
* Metadata sets (name, description) — requires the metadatamapping module
* Metadata set members (name, description, sort weight, metadata class, metadata uuid, metadata
  set uuid) — the owning metadata set and the referenced metadata item are pulled in via
  cross-domain closure; requires the metadatamapping module
* Metadata term mappings (mapping code, mapping source, metadata class name, metadata uuid) — the
  referenced metadata item is pulled in via cross-domain closure; requires the metadatamapping
  module
* Metadata sharing (raw zip packages already built and published through the metadatasharing
  module's own UI, copied out as-is; not CSV/XML — one file per package) — requires the
  metadatasharing module

Domains contributed by other modules (supportable, but depend on the module being present;
not yet covered):

* Identifier generation (idgen, auto-generation options)
* Address hierarchy (address hierarchy entries, location tag maps)
* Forms (Bahmni forms, AMPATH forms, AMPATH form translations, HTML forms)
* Billing / cashier (billable services, payment modes, cash points, cashier item prices)
* Appointment scheduling (specialities, service definitions, service types)
* Queues
* Cohorts (cohort types, cohort attribute types)
* FHIR (FHIR concept sources, FHIR patient identifier systems)
* Data filter mappings
* Dispositions
* OCL

Non-exportable Initializer domains (Liquibase, JSON key-values, system tasks) are
out of scope.

How it works
------------
On module startup the activator runs an export on a daemon thread (so it has full read access and
does not block startup). It writes to:

    <OpenMRS application data directory>/metadata_export/configuration/<domain>/...

The export is built in two separated stages:

1. Selection. Starting from seed objects, a `Selector` walks each object's dependencies to a
   fixpoint, producing an `ExportManifest` (the set of objects to export, bucketed by domain). This
   is what makes a package self-contained: for example, exporting a concept set also pulls in its
   members.
2. Export. Each `DomainExporter` writes its bucket in its own format. The service holds a registry
   of these and contains no per-domain logic.

Requirements
------------
The Initializer module must be installed (declared in `config.xml` `require_modules`); this module
reuses its `Domain` and CSV header definitions.

Adding a new domain
-------------------
Supporting a new metadata type is a new class plus one line in the registry, never a new method on
the service.

1. Write a `DomainExporter` and annotate it `@Component` so it is discovered automatically. For a
   CSV domain, extend `CsvDomainExporter<T>`:

```java
@Component
public class EncounterTypeDomainExporter extends CsvDomainExporter<EncounterType> {

    public Domain getDomain()               { return Domain.ENCOUNTER_TYPES; }

    public boolean handles(OpenmrsObject o) { return o instanceof EncounterType; }

    public Collection<EncounterType> getAllInstances() {
        return Context.getEncounterService().getAllEncounterTypes();
    }

    // Objects from OTHER domains this one references, for cross-domain closure. Empty if none.
    public Collection<? extends OpenmrsObject> getDependencies(EncounterType t) {
        return Collections.emptyList();
    }

    protected List<BaseLineExporter<EncounterType>> chain() {
        return Arrays.asList(new EncounterTypeLineExporter());
    }

    protected String fileName() { return "encounterTypes.csv"; }
}
```

2. Write the line exporter(s). Each writes header to value pairs into an `ExportLine`; it is the
   inverse of Initializer's matching `BaseLineProcessor.fill(...)`. Reuse Initializer's header
   constants where they are `public` so the two sides cannot drift. For the primary exporter of a
   domain, extend `MetadataLineExporter<T>`: it writes the uuid and the `void/retire` short-circuit
   for you, so `export` only handles the live, domain-specific columns:

```java
public class EncounterTypeLineExporter extends MetadataLineExporter<EncounterType> {
    public void export(EncounterType t, ExportLine line) {
        line.put(BaseLineProcessor.HEADER_NAME, t.getName());
        line.put(BaseLineProcessor.HEADER_DESC, t.getDescription());
    }
}
```

Exporters that only contribute extra columns to an existing row (not the primary one) extend
`BaseLineExporter<T>` directly instead.

A CSV domain may emit more than one file by overriding `partition(instances)` (the default is one
file).

For an XML domain (Initializer loads some domains, such as global properties, from XML rather than
CSV), extend `XmlDomainExporter<T>` instead of `CsvDomainExporter<T>`. Build the DOM in
`toDocuments(instances)` — keyed by file name so a domain can emit one file or many — and use the
inherited `newDocument()` to get a `Document` without JAXP boilerplate; the base handles indentation,
encoding, and placement under `configuration/<domain>/`:

```java
@Component
public class GlobalPropertyDomainExporter extends XmlDomainExporter<GlobalProperty> {

    public Domain getDomain()               { return Domain.GLOBAL_PROPERTIES; }

    public boolean handles(OpenmrsObject o) { return o instanceof GlobalProperty; }

    public Collection<GlobalProperty> getAllInstances() {
        return Context.getAdministrationService().getAllGlobalProperties();
    }

    // Objects from OTHER domains this one references, for cross-domain closure. Empty if none.
    public Collection<? extends OpenmrsObject> getDependencies(GlobalProperty gp) {
        return Collections.emptyList();
    }

    protected String fileName() { return "globalProperties.xml"; }

    protected Map<String, Document> toDocuments(Collection<GlobalProperty> gps) {
        Document doc = newDocument();
        // build <config><globalProperties><globalProperty>... and return
        return Collections.singletonMap(fileName(), doc);
    }
}
```

Any other non-CSV, non-XML domain (for example forms as JSON) skips both base classes and implements
`DomainExporter` directly, writing whatever files it likes in `export(bucket, context)`.

That is all. Because the exporter is a `@Component`, it is registered automatically; there is no
list to edit. Selection, closure, routing, and writing are handled by the framework.

Known limitations
-----------------
* Concept description UUIDs and index-term names are not round-trip-able (Initializer
  format/loader limitations), so they are not preserved or re-loadable.
* Selection currently exports all instances of the registered domains; instance-level seed
  selection is not yet exposed.
* Cross-domain closure only pulls in objects whose domain has a registered exporter.

Building from source
--------------------
Java 8+ and Maven. `mvn clean package` produces `omod/target/metadataexport-*.omod`. Code
formatting is handled by Spotless during the build (`mvn spotless:apply` to format manually).

Installation
------------
Build the `.omod`, then either upload it via Administration > Manage Modules or drop it into the
OpenMRS application data directory's `modules/` folder and restart. Ensure the Initializer module
is also installed.
