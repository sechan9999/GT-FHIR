package edu.gatech.i3l.fhir.dstu3.entities;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
//import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.dstu3.model.Observation.ObservationReferenceRangeComponent;
import org.hl7.fhir.dstu3.model.Observation.ObservationStatus;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.Type;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import ca.uhn.fhir.context.FhirVersionEnum;
//import ca.uhn.fhir.model.api.IDatatype;
//import ca.uhn.fhir.model.api.IResource;
//import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
//import ca.uhn.fhir.model.dstu2.composite.CodingDt;
//import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
//import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
//import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
//import ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt;
//import ca.uhn.fhir.model.dstu2.resource.Observation.Component;
//import ca.uhn.fhir.model.dstu2.resource.Observation.Related;
//import ca.uhn.fhir.model.dstu2.valueset.ObservationRelationshipTypeEnum;
//import ca.uhn.fhir.model.dstu2.valueset.ObservationStatusEnum;
//import ca.uhn.fhir.model.primitive.DateTimeDt;
//import ca.uhn.fhir.model.primitive.IdDt;
//import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.InstantDt;
import edu.gatech.i3l.fhir.jpa.dao.BaseFhirDao;
import edu.gatech.i3l.fhir.jpa.entity.BaseResourceEntity;
import edu.gatech.i3l.fhir.jpa.entity.IResourceEntity;
import edu.gatech.i3l.omop.enums.Omop4ConceptsFixedIds;
import edu.gatech.i3l.omop.mapping.OmopConceptMapping;

@Entity
@Table(name = "f_observation_view")
public class Observation extends BaseResourceEntity {

	private static final String RES_TYPE = "Observation";
	private static final ObservationStatus STATUS = ObservationStatus.FINAL;
	public static final Long SYSTOLIC_CONCEPT_ID = 3004249L;
	public static final Long DIASTOLIC_CONCEPT_ID = 3012888L;	

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "observation_id")
	@Access(AccessType.PROPERTY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "person_id", nullable = false)
	@NotNull
	private PersonComplement person;

	@ManyToOne(cascade = { CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinColumn(name = "observation_concept_id", nullable = false)
	@NotNull
	private Concept observationConcept;

	@Column(name = "observation_date", nullable = false)
	@Temporal(TemporalType.DATE)
	@NotNull
	private Date date;

	@Column(name = "observation_time")
	// @Temporal(TemporalType.TIME)
	private String time;

	@Column(name = "value_as_string")
	private String valueAsString;

	@Column(name = "value_as_number")
	private BigDecimal valueAsNumber;

	@Column(name = "range_low")
	private BigDecimal rangeLow;

	@Column(name = "range_high")
	private BigDecimal rangeHigh;

	@ManyToOne(cascade = { CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinColumn(name = "value_as_concept_id")
	private Concept valueAsConcept;

//	@ManyToOne(cascade = { CascadeType.MERGE }, fetch = FetchType.LAZY)
//	@JoinColumn(name = "relevant_condition_concept_id")
//	private Concept relevantCondition;

	@ManyToOne(cascade = { CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinColumn(name = "observation_type_concept_id", nullable = false)
	@NotNull
	private Concept type;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "provider_id")
	private Provider provider;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "visit_occurrence_id")
	private VisitOccurrence visitOccurrence;

	@Column(name = "source_value")
	private String sourceValue;

	@Column(name = "value_source_value")
	private String valueSourceValue;

	@ManyToOne(cascade = { CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinColumn(name = "unit_concept_id")
	private Concept unit;

	@Column(name = "unit_source_value")
	private String unitSourceValue;

	public Observation() {
		super();
		createDateTime();
	}

	public Observation(Long id, PersonComplement person, Concept observationConcept, Date date, String time, String valueAsString,
			BigDecimal valueAsNumber, Concept valueAsConcept, /*Concept relevantCondition,*/ Concept type,
			Provider provider, VisitOccurrence visitOccurrence, String sourceValue, Concept unit,
			String unitsSourceValue) {
		super();
		this.id = id;
		this.person = person;
		this.observationConcept = observationConcept;
		this.date = date;
		this.time = time;
		this.valueAsString = valueAsString;
		this.valueAsNumber = valueAsNumber;
		this.valueAsConcept = valueAsConcept;
//		this.relevantCondition = relevantCondition;
		this.type = type;
		this.provider = provider;
		this.visitOccurrence = visitOccurrence;
		this.sourceValue = sourceValue;
		this.unit = unit;
		this.unitSourceValue = unitsSourceValue;
		createDateTime();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public BigDecimal getRangeLow() {
		return rangeLow;
	}

	public void setRangeLow(BigDecimal rangeLow) {
		this.rangeLow = rangeLow;
	}

	public BigDecimal getRangeHigh() {
		return rangeHigh;
	}

	public void setRangeHigh(BigDecimal rangeHigh) {
		this.rangeHigh = rangeHigh;
	}

	public PersonComplement getPerson() {
		return person;
	}

	public void setPerson(PersonComplement person) {
		this.person = person;
	}

	public Concept getObservationConcept() {
		return observationConcept;
	}

	public void setObservationConcept(Concept observationConcept) {
		this.observationConcept = observationConcept;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getValueAsString() {
		return valueAsString;
	}

	public void setValueAsString(String valueAsString) {
		this.valueAsString = valueAsString;
	}

	public BigDecimal getValueAsNumber() {
		return valueAsNumber;
	}

	public void setValueAsNumber(BigDecimal valueAsNumber) {
		this.valueAsNumber = valueAsNumber;
	}

	public Concept getValueAsConcept() {
		return valueAsConcept;
	}

	public void setValueAsConcept(Concept valueAsConcept) {
		this.valueAsConcept = valueAsConcept;
	}

//	public Concept getRelevantCondition() {
//		return relevantCondition;
//	}
//
//	public void setRelevantCondition(Concept relevantCondition) {
//		this.relevantCondition = relevantCondition;
//	}

	public Concept getType() {
		return type;
	}

	public void setType(Concept type) {
		this.type = type;
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public VisitOccurrence getVisitOccurrence() {
		return visitOccurrence;
	}

	public void setVisitOccurrence(VisitOccurrence visitOccurrence) {
		this.visitOccurrence = visitOccurrence;
	}

	public String getSourceValue() {
		return sourceValue;
	}

	public void setSourceValue(String sourceValue) {
		this.sourceValue = sourceValue;
	}

	public Concept getUnit() {
		return unit;
	}

	public void setUnit(Concept unit) {
		this.unit = unit;
	}

	public String getUnitSourceValue() {
		return unitSourceValue;
	}

	public void setUnitSourceValue(String unitSourceValue) {
		this.unitSourceValue = unitSourceValue;
	}
	
	public String getValueSourceValue() {
		return valueSourceValue;
	}
	
	public void setValueSourceValue(String valueSourceValue) {
		this.valueSourceValue = valueSourceValue;
	}

	@Override
	public IResourceEntity constructEntityFromResource(IBaseResource resource) {
		System.out.println("Trying to write to Observation View Table");
		// TODO: This is view, which is read-only. We need to come up with a way to write
		// to either measurement or observation tables in OMOP. We may write them manually
		// and just return null for this. But then, response will not be correct. Revisit this.
		org.hl7.fhir.dstu3.model.Observation observation = (org.hl7.fhir.dstu3.model.Observation) resource;
		OmopConceptMapping ocm = OmopConceptMapping.getInstance();

		if (observation.getEffective() instanceof DateTimeType) {
			this.date = ((DateTimeType) observation.getEffective()).getValue();
			SimpleDateFormat timeFormat = new SimpleDateFormat ("HH:mm:ss");
			this.time = timeFormat.format(((DateTimeType) observation.getEffective()).getValue());
		} else if (observation.getEffective() instanceof Period) {
			// TODO: we need to handle period. We can probably use
			// we can use range_low and range_high. These are only available in Measurement
		}

		/*
		 * Set subject: currently supporting only type Person TODO create
		 * entity-complement to specify other types of subjects
		 */
		Reference subjectReference = observation.getSubject();
		if (subjectReference != null && !subjectReference.isEmpty()) {
			if ("Patient".equals(subjectReference.getReferenceElement().getResourceType())) {
				this.person = new PersonComplement();
				this.person.setId(subjectReference.getReferenceElement().getIdPartAsLong());
			} else if ("Group".equals(subjectReference.getReferenceElement().getResourceType())) {
				//
			} else if ("Device".equals(subjectReference.getReferenceElement().getResourceType())) {
				//
			} else if ("Location".equals(subjectReference.getReferenceElement().getResourceType())) {
				//
			}
		}

		/* Set visit occurrence */
		Reference visitRef = observation.getContext();
		if (visitRef != null && !visitRef.isEmpty()) {
			this.visitOccurrence = new VisitOccurrence();
			this.visitOccurrence.setId(visitRef.getReferenceElement().getIdPartAsLong());
		}

		Long observationConceptId = ocm.get(observation.getCode().getCodingFirstRep().getCode(),
				OmopConceptMapping.LOINC_CODE);
		if (observationConceptId != null) {
			this.observationConcept = new Concept();
			this.observationConcept.setId(observationConceptId);
		}

		/* Set the type of the observation */
		this.type = new Concept();
		if (observation.getMethod().getCodingFirstRep() != null) {
			this.type.setId(Omop4ConceptsFixedIds.OBSERVATION_FROM_LAB_NUMERIC_RESULT.getConceptId()); // assuming
																										// all
																										// results
																										// on
																										// this
																										// table
																										// are
																										// quantitative:
																										// http://hl7.org/fhir/2015May/valueset-observation-methods.html
		} else {
			this.type.setId(Omop4ConceptsFixedIds.OBSERVATION_FROM_EHR.getConceptId());
		}

		/* Set the value of the observation */
		Type value = observation.getValue();
		if (value instanceof Quantity) {
			Long unitId = ocm.get(((Quantity) value).getUnit(), OmopConceptMapping.UCUM_CODE,
					OmopConceptMapping.UCUM_CODE_STANDARD, OmopConceptMapping.UCUM_CODE_CUSTOM);
			this.valueAsNumber = ((Quantity) value).getValue();
			if (unitId != null) {
				this.unit = new Concept();
				this.unit.setId(unitId);
			}
			this.rangeHigh = observation.getReferenceRangeFirstRep().getHigh().getValue();
			this.rangeLow = observation.getReferenceRangeFirstRep().getLow().getValue();
		} else if (value instanceof CodeableConcept) {
			Long valueAsConceptId = ocm.get(((CodeableConcept) value).getCodingFirstRep().getCode(),
					OmopConceptMapping.CLINICAL_FINDING);
			if (valueAsConceptId != null) {
				this.valueAsConcept = new Concept();
				this.valueAsConcept.setId(valueAsConceptId);
			}
		} else {
			this.valueAsString = ((StringType) value).getValue();
		}

		// quick solution.
		this.sourceValue = "NA";

		return this;
	}

	@Override
	public FhirVersionEnum getFhirVersion() {
		return FhirVersionEnum.DSTU2;
	}

	@Override
	public org.hl7.fhir.dstu3.model.Observation getRelatedResource() {
		org.hl7.fhir.dstu3.model.Observation observation = new org.hl7.fhir.dstu3.model.Observation();
		observation.setId(this.getIdDt());

		String systemUriString = this.getObservationConcept().getVocabulary().getSystemUri();
		String codeString = this.getObservationConcept().getConceptCode();
		String displayString;
		if (this.getObservationConcept().getId() == 0L) {
			displayString = this.getSourceValue();
		} else {
			displayString = this.getObservationConcept().getName();
		}
		
		// OMOP database maintains Systolic and Diastolic Blood Pressures separately.
		// FHIR however keeps them together. Observation DAO filters out Diastolic values.
		// Here, when we are reading systolic, we search for matching diastolic and put them
		// together. The Observation ID will be systolic's OMOP ID. 
		// public static final Long SYSTOLIC_CONCEPT_ID = new Long(3004249);
		// public static final Long DIASTOLIC_CONCEPT_ID = new Long(3012888);		
		if (SYSTOLIC_CONCEPT_ID.equals(this.observationConcept.getId())) {
			// Set coding for systolic and diastolic observation
			Coding obsCoding = new Coding("http://loinc.org", "55284-4", "Blood pressure systolic & diastolic");
			CodeableConcept obsCode = new CodeableConcept();
			obsCode.addCoding(obsCoding);
			observation.setCode(obsCode);
			
			// First we add systolic component.
			ObservationComponentComponent obsComponent1 = new ObservationComponentComponent();
			
			CodeableConcept component1Code = new CodeableConcept();
			Coding component1CodeCoding = new Coding(systemUriString, codeString, displayString);
			component1Code.addCoding(component1CodeCoding);
			obsComponent1.setCode(component1Code);
			
			Type compValue = null;
			if (this.valueAsNumber != null) {
				Quantity quantity = new Quantity(this.valueAsNumber.doubleValue());
				// Unit is defined as a concept code in omop v4, then unit and code are the same in this case
				if (this.unit != null) {
					quantity.setUnit(this.unit.getConceptCode());
					quantity.setCode(this.unit.getConceptCode());
					quantity.setSystem(this.unit.getVocabulary().getSystemUri());
				}
				obsComponent1.setValue(quantity);
			}
			
			observation.addComponent(obsComponent1);
			
			// Now search for diastolic component.
			WebApplicationContext myAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
			EntityManager entityManager = myAppCtx.getBean("myBaseDao", BaseFhirDao.class).getEntityManager();

			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Observation> criteria = builder.createQuery(Observation.class);
			Root<Observation> from = criteria.from(Observation.class);
			criteria.select(from).where(
					builder.equal(from.get("observationConcept").get("id"), DIASTOLIC_CONCEPT_ID),
					builder.equal(from.get("person").get("id"), this.person.getId()),
					builder.equal(from.get("date"), this.date),
					builder.equal(from.get("time"),  this.time)
					);
			TypedQuery<Observation> query = entityManager.createQuery(criteria);
			List<Observation> results = query.getResultList();
			if (results.size() > 0) {
				Observation diastolicOb = results.get(0);
				ObservationComponentComponent obsComponent2 = new ObservationComponentComponent();

				// Diastolic component found. Set code.
				CodeableConcept component2Code = new CodeableConcept();
				Coding component2CodeCoding = new Coding(diastolicOb.getObservationConcept().getVocabulary().getSystemUri(),
						diastolicOb.getObservationConcept().getConceptCode(),
						diastolicOb.getObservationConcept().getName());
				component2Code.addCoding(component2CodeCoding);
				obsComponent2.setCode(component2Code);
				
				if (diastolicOb.valueAsNumber != null) {
					Quantity quantity = new Quantity(diastolicOb.valueAsNumber.doubleValue());
					// Unit is defined as a concept code in omop v4, then unit and code are the same in this case
					if (diastolicOb.unit != null) {
						quantity.setUnit(diastolicOb.unit.getConceptCode());
						quantity.setCode(diastolicOb.unit.getConceptCode());
						quantity.setSystem(diastolicOb.unit.getVocabulary().getSystemUri());
					}
					obsComponent2.setValue(quantity);
				}
				
				observation.addComponent(obsComponent2);
			}			
		} else {
			// Set Observation Code.
			CodeableConcept obsCode = new CodeableConcept();
			Coding obsCodeCoding = new Coding(systemUriString, codeString, displayString);
			obsCode.addCoding(obsCodeCoding);
			observation.setCode(obsCode);
			
			Type value = null;
			if (this.valueAsNumber != null) {
				Quantity quantity = new Quantity(this.valueAsNumber.doubleValue());
				if (this.unit != null) {
					// Unit is defined as a concept code in omop v4, then unit and code are the same in this case				
					quantity.setUnit(this.unit.getConceptCode());
					quantity.setCode(this.unit.getConceptCode());
					quantity.setSystem(this.unit.getVocabulary().getSystemUri());
				}
				value = quantity;
			} else if (this.valueAsString != null) {
				value = new StringType(this.valueAsString);
			} else if (this.valueAsConcept != null && this.valueAsConcept.getId() != 0L) {
				// vocabulary is a required attribute for concept, then it's expected to not be null
				CodeableConcept valueCode = new CodeableConcept();
				Coding valueCodeCoding = new Coding(this.valueAsConcept.getVocabulary().getSystemUri(),
						this.valueAsConcept.getConceptCode(),
						this.valueAsConcept.getName());
				valueCode.addCoding(valueCodeCoding);
				value = valueCode;
			} else {
				value = new StringType(this.getValueSourceValue());
			}
			observation.setValue(value);
		}

		ObservationReferenceRangeComponent obsRefRangeComp = new ObservationReferenceRangeComponent();
		if (this.rangeLow != null) {
			SimpleQuantity lowSimpleQty = new SimpleQuantity();
			lowSimpleQty.setValue(this.rangeLow.doubleValue());
			obsRefRangeComp.setLow(lowSimpleQty);
		}
		if (this.rangeHigh != null) {
			SimpleQuantity highSimpleQty = new SimpleQuantity();
			highSimpleQty.setValue(this.rangeHigh.doubleValue());
			obsRefRangeComp.setHigh(highSimpleQty);
		}
		if (!obsRefRangeComp.isEmpty()) {
			observation.addReferenceRange(obsRefRangeComp);
		}
		
		observation.setStatus(STATUS);

		if (this.date != null) {
//			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
//			String dateString = fmt.format(this.date);
//			fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			Date myDate = null;
//			try {
//				if (this.time != null && this.time.isEmpty() == false) {
//					myDate = fmt.parse(dateString+" "+this.time);
//				} else {
//					myDate = this.date;
//				}
//			} catch (ParseException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
			Date myDate = createDateTime();			
			if (myDate != null) {
				DateTimeType appliesDate = new DateTimeType(myDate);
				observation.setEffective(appliesDate);
			}
		}
//		if (// this.date != null &&
//		this.time != null) { // WARNING notice that the resource field
//								// 'appliesDate' relies only on the entity field
//								// 'time'
//			DateTimeDt appliesDate = new DateTimeDt(this.time);
//			observation.setEffective(appliesDate);
//		}
		
		if (this.person != null) {
			Reference patientRef = new Reference(Person.RES_TYPE+"/"+this.getPerson().getId());
			patientRef.setDisplay(this.getPerson().getNameAsSingleString());
			observation.setSubject(patientRef);
		}
		
		if (this.visitOccurrence != null) {
			Reference visitRef = new Reference(VisitOccurrence.RES_TYPE+"/"+this.getVisitOccurrence().getId());
			observation.setContext(visitRef);
		}
		
		if (this.type != null) {
			try {
				CodeableConcept typeCode = new CodeableConcept();
				ObservationStatus obsStatus = null;
				if (this.type.getId() == 44818701L || this.type.getId() == 38000280L || this.type.getId() == 38000281L) {
				// This is From physical examination.
					obsStatus = ObservationStatus.fromCode("exam");
				} else if (this.type.getId() == 44818702L || this.type.getId() == 38000277L || this.type.getId() == 38000278L) {
					obsStatus = ObservationStatus.fromCode("laboratory");
				} else if (this.type.getId() == 45905771L) {
					obsStatus = ObservationStatus.fromCode("survey");
				}
				if (obsStatus != null) {
					Coding typeCodeCoding = new Coding(obsStatus.getSystem(), obsStatus.toCode(), obsStatus.getDisplay());
					typeCode.addCoding(typeCodeCoding);
					observation.addCategory(typeCode);
				}
			} catch (FHIRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		
		return observation;
	}

	@Override
	public String getResourceType() {
		return RES_TYPE;
	}

	@Override
	public InstantDt getUpdated() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String translateSearchParam(String theSearchParam) {
		System.out.println("Observation Search:"+theSearchParam);
		switch (theSearchParam) {
		case ca.uhn.fhir.model.dstu2.resource.Observation.SP_SUBJECT:
			return "person";
		case ca.uhn.fhir.model.dstu2.resource.Observation.SP_PATIENT:
			return "person";
		case ca.uhn.fhir.model.dstu2.resource.Observation.SP_ENCOUNTER:
			return "visitOccurrence";
		case ca.uhn.fhir.model.dstu2.resource.Observation.SP_VALUE_QUANTITY:
			return "valueAsNumber";
		case ca.uhn.fhir.model.dstu2.resource.Observation.SP_VALUE_STRING:
			return "valueAsString";
		case ca.uhn.fhir.model.dstu2.resource.Observation.SP_VALUE_CONCEPT:
			return "valueAsConcept";
		case ca.uhn.fhir.model.dstu2.resource.Observation.SP_DATE:
			return "date";
		default:
			break;
		}
		return theSearchParam;
	}

	private Date createDateTime() {
		Date myDate = null;
		if (this.date != null) {
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
			String dateString = fmt.format(this.date);
			fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				if (this.time != null && this.time.isEmpty() == false) {
					myDate = fmt.parse(dateString+" "+this.time);
				} else {
					myDate = this.date;
				}				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return myDate;
	}
}
