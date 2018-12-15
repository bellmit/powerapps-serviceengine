package com.profitera.persistence.impl;

import oracle.toplink.mappings.DirectToFieldMapping;
import oracle.toplink.mappings.ManyToManyMapping;
import oracle.toplink.mappings.OneToManyMapping;
import oracle.toplink.mappings.OneToOneMapping;
import oracle.toplink.publicinterface.Descriptor;

import com.profitera.persistence.PASWorkBench;

public class CustomerMapping {
  public static Descriptor build(PASWorkBench p) {
    Descriptor descriptor = new Descriptor();
    descriptor.setJavaClass(com.profitera.descriptor.db.account.Customer.class);
    descriptor.addTableName("PTRCUSTOMER");
    descriptor.addPrimaryKeyFieldName("PTRCUSTOMER.CUSTOMER_ID");

    // Descriptor properties.
    descriptor.useSoftCacheWeakIdentityMap();
    descriptor.setIdentityMapSize(100);
    descriptor.useRemoteSoftCacheWeakIdentityMap();
    descriptor.setRemoteIdentityMapSize(100);
    descriptor.setAlias("Ptrcustomer");

    // Query manager.
    descriptor.getQueryManager().checkCacheForDoesExist();
    //Named Queries

    // Event manager.

    // Mappings.
    DirectToFieldMapping businessRegNumberMapping = new DirectToFieldMapping();
    businessRegNumberMapping.setAttributeName("businessRegNumber");
    businessRegNumberMapping.setFieldName("PTRCUSTOMER.BUSINESS_REG_NUMBER");
    descriptor.addMapping(businessRegNumberMapping);

    DirectToFieldMapping custCategoryMapping = new DirectToFieldMapping();
    custCategoryMapping.setAttributeName("custCategory");
    custCategoryMapping.setFieldName("PTRCUSTOMER.CUST_CATEGORY");
    descriptor.addMapping(custCategoryMapping);

    DirectToFieldMapping custSubSegmentIdMapping = new DirectToFieldMapping();
    custSubSegmentIdMapping.setAttributeName("custSubSegmentId");
    custSubSegmentIdMapping.setFieldName("PTRCUSTOMER.CUST_SUB_SEGMENT_ID");
    descriptor.addMapping(custSubSegmentIdMapping);

    DirectToFieldMapping customerIdMapping = new DirectToFieldMapping();
    customerIdMapping.setAttributeName("customerId");
    customerIdMapping.setFieldName("PTRCUSTOMER.CUSTOMER_ID");
    descriptor.addMapping(customerIdMapping);

    DirectToFieldMapping customerLockedTimeMapping = new DirectToFieldMapping();
    customerLockedTimeMapping.setAttributeName("customerLockedTime");
    customerLockedTimeMapping.setFieldName("PTRCUSTOMER.CUSTOMER_LOCKED_TIME");
    descriptor.addMapping(customerLockedTimeMapping);

    DirectToFieldMapping employerNameMapping = new DirectToFieldMapping();
    employerNameMapping.setAttributeName("employerName");
    employerNameMapping.setFieldName("PTRCUSTOMER.EMPLOYER_NAME");
    descriptor.addMapping(employerNameMapping);

    DirectToFieldMapping industryMapping = new DirectToFieldMapping();
    industryMapping.setAttributeName("industry");
    industryMapping.setFieldName("PTRCUSTOMER.INDUSTRY");
    descriptor.addMapping(industryMapping);

    DirectToFieldMapping noTimesFraudMapping = new DirectToFieldMapping();
    noTimesFraudMapping.setAttributeName("noTimesFraud");
    noTimesFraudMapping.setFieldName("PTRCUSTOMER.NO_TIMES_F");
    descriptor.addMapping(noTimesFraudMapping);

    DirectToFieldMapping noTimesLostMapping = new DirectToFieldMapping();
    noTimesLostMapping.setAttributeName("noTimesLost");
    noTimesLostMapping.setFieldName("PTRCUSTOMER.NO_TIMES_L");
    descriptor.addMapping(noTimesLostMapping);

    DirectToFieldMapping noTimesStolenMapping = new DirectToFieldMapping();
    noTimesStolenMapping.setAttributeName("noTimesStolen");
    noTimesStolenMapping.setFieldName("PTRCUSTOMER.NO_TIMES_S");
    descriptor.addMapping(noTimesStolenMapping);

    DirectToFieldMapping priorityCustomerIndMapping = new DirectToFieldMapping();
    priorityCustomerIndMapping.setAttributeName("priorityCustomerInd");
    priorityCustomerIndMapping
        .setFieldName("PTRCUSTOMER.PRIORITY_CUSTOMER_IND");
    descriptor.addMapping(priorityCustomerIndMapping);
    
    DirectToFieldMapping recoveryPotentialAmountMapping = new DirectToFieldMapping();
    recoveryPotentialAmountMapping.setAttributeName("recoveryPotentialAmount");
    recoveryPotentialAmountMapping.setFieldName("PTRCUSTOMER.RECOVERY_POTENTIAL_AMOUNT");
    descriptor.addMapping(recoveryPotentialAmountMapping);

    DirectToFieldMapping availableCreditMapping = new DirectToFieldMapping();
    availableCreditMapping.setAttributeName("availableCredit");
    availableCreditMapping.setFieldName("PTRCUSTOMER.AVAILABLE_CREDIT");
    descriptor.addMapping(availableCreditMapping);    

    DirectToFieldMapping availableCashMapping = new DirectToFieldMapping();
    availableCashMapping.setAttributeName("availableCash");
    availableCashMapping.setFieldName("PTRCUSTOMER.AVAILABLE_CASH");
    descriptor.addMapping(availableCashMapping);        

    DirectToFieldMapping priorityBankFlagMapping = new DirectToFieldMapping();
    priorityBankFlagMapping.setAttributeName("priorityBankFlag");
    priorityBankFlagMapping.setFieldName("PTRCUSTOMER.PRIORITY_BANK_FLAG");
    descriptor.addMapping(priorityBankFlagMapping);           
    
    OneToOneMapping addressDetailsMapping = new OneToOneMapping();
    addressDetailsMapping.setAttributeName("addressDetails");
    addressDetailsMapping.setReferenceClass(com.profitera.descriptor.db.contact.AddressDetails.class);
    addressDetailsMapping.useBasicIndirection();
    addressDetailsMapping.addForeignKeyFieldName("PTRCUSTOMER.CONTACT_ID",
        "PTRADDRESS_DET.CONTACT_ID");
    descriptor.addMapping(addressDetailsMapping);

    OneToOneMapping clientMapping = new OneToOneMapping();
    clientMapping.setAttributeName("client");
    clientMapping
        .setReferenceClass(com.profitera.descriptor.db.client.Client.class);
    clientMapping.useBasicIndirection();
    clientMapping.addForeignKeyFieldName("PTRCUSTOMER.CLIENT_ID",
        "PTRCLIENT.CLIENT_ID");
    descriptor.addMapping(clientMapping);

    OneToOneMapping customerSegmentMapping = new OneToOneMapping();
    customerSegmentMapping.setAttributeName("customerSegment");
    customerSegmentMapping
        .setReferenceClass(com.profitera.descriptor.db.account.CustomerSegment.class);
    customerSegmentMapping.useBasicIndirection();
    customerSegmentMapping.addForeignKeyFieldName(
        "PTRCUSTOMER.CUST_SEGMENT_ID", "PTRCUSTOMER_SEGMENT.CUST_SEGMENT_ID");
    descriptor.addMapping(customerSegmentMapping);

    OneToOneMapping employerBusinessRefMapping = new OneToOneMapping();
    employerBusinessRefMapping.setAttributeName("employerBusinessRef");
    employerBusinessRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.EmployerBusinessRef.class);
    employerBusinessRefMapping.useBasicIndirection();
    employerBusinessRefMapping.addForeignKeyFieldName(
        "PTRCUSTOMER.EMPLOYER_BUSINESS_ID",
        "PTREMPLOYER_BUSINESS_REF.EMPLOYER_BUSINESS_ID");
    descriptor.addMapping(employerBusinessRefMapping);

    OneToOneMapping employmentTypeRefMapping = new OneToOneMapping();
    employmentTypeRefMapping.setAttributeName("employmentTypeRef");
    employmentTypeRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.EmploymentTypeRef.class);
    employmentTypeRefMapping.useBasicIndirection();
    employmentTypeRefMapping.addForeignKeyFieldName(
        "PTRCUSTOMER.EMPLOYMENT_TYPE_ID",
        "PTREMPLOYMENT_TYPE_REF.EMPLOYMENT_TYPE_ID");
    descriptor.addMapping(employmentTypeRefMapping);

    OneToOneMapping parentCustomerMapping = new OneToOneMapping();
    parentCustomerMapping.setAttributeName("parentCustomer");
    parentCustomerMapping.setReferenceClass(com.profitera.descriptor.db.account.Customer.class);
    parentCustomerMapping.useBasicIndirection();
    parentCustomerMapping.addForeignKeyFieldName(
        "PTRCUSTOMER.CUSTOMER_PARENT_ID", "PTRCUSTOMER.CUSTOMER_ID");
    descriptor.addMapping(parentCustomerMapping);

    OneToOneMapping profileSegmentRefMapping = new OneToOneMapping();
    profileSegmentRefMapping.setAttributeName("profileSegmentRef");
    profileSegmentRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.ProfileSegmentRef.class);
    profileSegmentRefMapping.useBasicIndirection();
    profileSegmentRefMapping.addForeignKeyFieldName(
        "PTRCUSTOMER.PROFILE_SEGMENT_ID", "PTRPROFILE_SEGMENT_REF.PROFILE_ID");
    descriptor.addMapping(profileSegmentRefMapping);

    OneToOneMapping treatmentStageRefMapping = new OneToOneMapping();
    treatmentStageRefMapping.setAttributeName("treatmentStageRef");
    treatmentStageRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.TreatmentStageRef.class);
    treatmentStageRefMapping.useBasicIndirection();
    treatmentStageRefMapping.addForeignKeyFieldName(
        "PTRCUSTOMER.CUSTOMER_TREATMENT_STAGE",
        "PTRTREATMENT_STAGE_REF.TREATMENT_STAGE_ID");
    descriptor.addMapping(treatmentStageRefMapping);

    OneToOneMapping userMapping = new OneToOneMapping();
    userMapping.setAttributeName("user");
    userMapping.setReferenceClass(com.profitera.descriptor.db.user.User.class);
    userMapping.useBasicIndirection();
    userMapping
        .addForeignKeyFieldName("PTRCUSTOMER.USER_ID", "PTRUSER.USER_ID");
    descriptor.addMapping(userMapping);

    OneToOneMapping workListStatusRefMapping = new OneToOneMapping();
    workListStatusRefMapping.setAttributeName("workListStatusRef");
    workListStatusRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.AccountWorkListStatusRef.class);
    workListStatusRefMapping.useBasicIndirection();
    workListStatusRefMapping.addForeignKeyFieldName(
        "PTRCUSTOMER.WORK_LIST_STATUS_ID",
        "PTRACC_WRKLIS_STATUS_REF.ACC_WRKLIST_STATUS_ID");
    descriptor.addMapping(workListStatusRefMapping);

    OneToOneMapping worklistMapping = new OneToOneMapping();
    worklistMapping.setAttributeName("worklist");
    worklistMapping
        .setReferenceClass(com.profitera.descriptor.db.worklist.WorkList.class);
    worklistMapping.useBasicIndirection();
    worklistMapping.addForeignKeyFieldName("PTRCUSTOMER.WORK_LIST_ID",
        "PTRWORK_LIST.WORK_LIST_ID");
    descriptor.addMapping(worklistMapping);

    OneToManyMapping accountMapping = new OneToManyMapping();
    accountMapping.setAttributeName("account");
    accountMapping
        .setReferenceClass(com.profitera.descriptor.db.account.Account.class);
    accountMapping.useBasicIndirection();
    accountMapping.addTargetForeignKeyFieldName("PTRACCOUNT.CUSTOMER_ID",
        "PTRCUSTOMER.CUSTOMER_ID");
    descriptor.addMapping(accountMapping);

    OneToManyMapping childCustomersMapping = new OneToManyMapping();
    childCustomersMapping.setAttributeName("childCustomers");
    childCustomersMapping
        .setReferenceClass(com.profitera.descriptor.db.account.Customer.class);
    childCustomersMapping.useBasicIndirection();
    childCustomersMapping.addTargetForeignKeyFieldName(
        "PTRCUSTOMER.CUSTOMER_PARENT_ID", "PTRCUSTOMER.CUSTOMER_ID");
    descriptor.addMapping(childCustomersMapping);

    ManyToManyMapping contactsMapping = new ManyToManyMapping();
    contactsMapping.setAttributeName("contacts");
    contactsMapping
        .setReferenceClass(com.profitera.descriptor.db.contact.AddressDetails.class);
    contactsMapping.useBasicIndirection();
    contactsMapping.setRelationTableName("PTRCUSTOMER_CONTACT_REL");
    contactsMapping.addSourceRelationKeyFieldName(
        "PTRCUSTOMER_CONTACT_REL.CUSTOMER_ID", "PTRCUSTOMER.CUSTOMER_ID");
    contactsMapping.addTargetRelationKeyFieldName(
        "PTRCUSTOMER_CONTACT_REL.CONTACT_ID", "PTRADDRESS_DET.CONTACT_ID");
    descriptor.addMapping(contactsMapping);
    return descriptor;
  }

}
