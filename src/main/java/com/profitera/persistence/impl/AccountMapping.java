package com.profitera.persistence.impl;

import com.profitera.persistence.PASWorkBench;

import oracle.toplink.mappings.DirectToFieldMapping;
import oracle.toplink.mappings.ManyToManyMapping;
import oracle.toplink.mappings.OneToManyMapping;
import oracle.toplink.mappings.OneToOneMapping;
import oracle.toplink.publicinterface.Descriptor;

public class AccountMapping {
  public static Descriptor build(PASWorkBench p) {
    Descriptor descriptor = new Descriptor();
    descriptor.setJavaClass(com.profitera.descriptor.db.account.Account.class);
    descriptor.addTableName("PTRACCOUNT");
    descriptor.addPrimaryKeyFieldName("PTRACCOUNT.ACCOUNT_ID");

    // Descriptor properties.
    descriptor.useSoftCacheWeakIdentityMap();
    descriptor.setIdentityMapSize(100);
    descriptor.useRemoteSoftCacheWeakIdentityMap();
    descriptor.setRemoteIdentityMapSize(100);
    descriptor.setSequenceNumberFieldName("PTRACCOUNT.ACCOUNT_ID");
    descriptor.setSequenceNumberName("ACCOUNT_ID_SEQ");
    descriptor.alwaysConformResultsInUnitOfWork();
    descriptor.setAlias("Ptraccount");

    // Query manager.
    descriptor.getQueryManager().checkCacheForDoesExist();
    //Named Queries

    // Event manager.

    // Mappings.
    DirectToFieldMapping accountIdMapping = new DirectToFieldMapping();
    accountIdMapping.setAttributeName("accountId");
    accountIdMapping.setFieldName("PTRACCOUNT.ACCOUNT_ID");
    descriptor.addMapping(accountIdMapping);

    DirectToFieldMapping accountNumberMapping = new DirectToFieldMapping();
    accountNumberMapping.setAttributeName("accountNumber");
    accountNumberMapping.setFieldName("PTRACCOUNT.ACCOUNT_NUMBER");
    descriptor.addMapping(accountNumberMapping);

    DirectToFieldMapping acquisitionChannelMapping = new DirectToFieldMapping();
    acquisitionChannelMapping.setAttributeName("acquisitionChannel");
    acquisitionChannelMapping.setFieldName("PTRACCOUNT.ACQUISITION_CHANNEL");
    descriptor.addMapping(acquisitionChannelMapping);

    DirectToFieldMapping agreementDateMapping = new DirectToFieldMapping();
    agreementDateMapping.setAttributeName("agreementDate");
    agreementDateMapping.setFieldName("PTRACCOUNT.AGREEMENT_DATE");
    descriptor.addMapping(agreementDateMapping);

    DirectToFieldMapping agreementFilenoMapping = new DirectToFieldMapping();
    agreementFilenoMapping.setAttributeName("agreementFileno");
    agreementFilenoMapping.setFieldName("PTRACCOUNT.AGREEMENT_FILENO");
    descriptor.addMapping(agreementFilenoMapping);

    DirectToFieldMapping agreementLocationMapping = new DirectToFieldMapping();
    agreementLocationMapping.setAttributeName("agreementLocation");
    agreementLocationMapping.setFieldName("PTRACCOUNT.AGREEMENT_LOCATION");
    descriptor.addMapping(agreementLocationMapping);

    DirectToFieldMapping authorisedBranchMapping = new DirectToFieldMapping();
    authorisedBranchMapping.setAttributeName("authorisedBranch");
    authorisedBranchMapping.setFieldName("PTRACCOUNT.AUTHORISED_BRANCH");
    descriptor.addMapping(authorisedBranchMapping);

    DirectToFieldMapping authorisedEmployeeMapping = new DirectToFieldMapping();
    authorisedEmployeeMapping.setAttributeName("authorisedEmployee");
    authorisedEmployeeMapping.setFieldName("PTRACCOUNT.AUTHORISED_EMPLOYEE");
    descriptor.addMapping(authorisedEmployeeMapping);

    DirectToFieldMapping authorisedNameMapping = new DirectToFieldMapping();
    authorisedNameMapping.setAttributeName("authorisedName");
    authorisedNameMapping.setFieldName("PTRACCOUNT.AUTHORISED_NAME");
    descriptor.addMapping(authorisedNameMapping);

    DirectToFieldMapping behaviourScoreMapping = new DirectToFieldMapping();
    behaviourScoreMapping.setAttributeName("behaviourScore");
    behaviourScoreMapping.setFieldName("PTRACCOUNT.BEHAVIOUR_SCORE");
    descriptor.addMapping(behaviourScoreMapping);

    DirectToFieldMapping blacklistedMapping = new DirectToFieldMapping();
    blacklistedMapping.setAttributeName("blacklisted");
    blacklistedMapping.setFieldName("PTRACCOUNT.BLACKLISTED");
    descriptor.addMapping(blacklistedMapping);

    DirectToFieldMapping cardExpiryMapping = new DirectToFieldMapping();
    cardExpiryMapping.setAttributeName("cardExpiry");
    cardExpiryMapping.setFieldName("PTRACCOUNT.CARD_EXPIRY");
    descriptor.addMapping(cardExpiryMapping);

    DirectToFieldMapping chargeoffAmtMapping = new DirectToFieldMapping();
    chargeoffAmtMapping.setAttributeName("chargeoffAmt");
    chargeoffAmtMapping.setFieldName("PTRACCOUNT.CHARGEOFF_AMT");
    descriptor.addMapping(chargeoffAmtMapping);

    DirectToFieldMapping chargeoffDateMapping = new DirectToFieldMapping();
    chargeoffDateMapping.setAttributeName("chargeoffDate");
    chargeoffDateMapping.setFieldName("PTRACCOUNT.CHARGEOFF_DATE");
    descriptor.addMapping(chargeoffDateMapping);

    DirectToFieldMapping contractExpiryDateMapping = new DirectToFieldMapping();
    contractExpiryDateMapping.setAttributeName("contractExpiryDate");
    contractExpiryDateMapping.setFieldName("PTRACCOUNT.CONTRACT_EXPIRY_DATE");
    descriptor.addMapping(contractExpiryDateMapping);

    DirectToFieldMapping creditLimitMapping = new DirectToFieldMapping();
    creditLimitMapping.setAttributeName("creditLimit");
    creditLimitMapping.setFieldName("PTRACCOUNT.CREDIT_LIMIT");
    descriptor.addMapping(creditLimitMapping);

    DirectToFieldMapping creditScoreMapping = new DirectToFieldMapping();
    creditScoreMapping.setAttributeName("creditScore");
    creditScoreMapping.setFieldName("PTRACCOUNT.CREDIT_SCORE");
    descriptor.addMapping(creditScoreMapping);

    DirectToFieldMapping currDueAmtMapping = new DirectToFieldMapping();
    currDueAmtMapping.setAttributeName("currDueAmt");
    currDueAmtMapping.setFieldName("PTRACCOUNT.CURR_DUE_AMT");
    descriptor.addMapping(currDueAmtMapping);

    DirectToFieldMapping cycDelIdMapping = new DirectToFieldMapping();
    cycDelIdMapping.setAttributeName("cycDelId");
    cycDelIdMapping.setFieldName("PTRACCOUNT.CYC_DEL_ID");
    descriptor.addMapping(cycDelIdMapping);

    DirectToFieldMapping dateInCollectionMapping = new DirectToFieldMapping();
    dateInCollectionMapping.setAttributeName("dateInCollection");
    dateInCollectionMapping.setFieldName("PTRACCOUNT.DATE_IN_COLLECTION");
    descriptor.addMapping(dateInCollectionMapping);

    DirectToFieldMapping depositAmtMapping = new DirectToFieldMapping();
    depositAmtMapping.setAttributeName("depositAmt");
    depositAmtMapping.setFieldName("PTRACCOUNT.DEPOSIT_AMT");
    descriptor.addMapping(depositAmtMapping);

    DirectToFieldMapping externalProfilingScoreMapping = new DirectToFieldMapping();
    externalProfilingScoreMapping.setAttributeName("externalProfilingScore");
    externalProfilingScoreMapping
        .setFieldName("PTRACCOUNT.EXTERNAL_PROFILING_SCORE");
    descriptor.addMapping(externalProfilingScoreMapping);

    DirectToFieldMapping firstChargeoffAmtMapping = new DirectToFieldMapping();
    firstChargeoffAmtMapping.setAttributeName("firstChargeoffAmt");
    firstChargeoffAmtMapping.setFieldName("PTRACCOUNT.FIRST_CHARGEOFF_AMT");
    descriptor.addMapping(firstChargeoffAmtMapping);

    DirectToFieldMapping hiBalanceMapping = new DirectToFieldMapping();
    hiBalanceMapping.setAttributeName("hiBalance");
    hiBalanceMapping.setFieldName("PTRACCOUNT.HI_BALANCE");
    descriptor.addMapping(hiBalanceMapping);

    DirectToFieldMapping industryCategoryMapping = new DirectToFieldMapping();
    industryCategoryMapping.setAttributeName("industryCategory");
    industryCategoryMapping.setFieldName("PTRACCOUNT.INDUSTRY_CATEGORY");
    descriptor.addMapping(industryCategoryMapping);

    DirectToFieldMapping interestOverdueMapping = new DirectToFieldMapping();
    interestOverdueMapping.setAttributeName("interestOverdue");
    interestOverdueMapping.setFieldName("PTRACCOUNT.INTEREST_OVERDUE");
    descriptor.addMapping(interestOverdueMapping);

    DirectToFieldMapping lastPaymtAmtMapping = new DirectToFieldMapping();
    lastPaymtAmtMapping.setAttributeName("lastPaymtAmt");
    lastPaymtAmtMapping.setFieldName("PTRACCOUNT.LAST_PAYMT_AMT");
    descriptor.addMapping(lastPaymtAmtMapping);

    DirectToFieldMapping lastPaymtDateMapping = new DirectToFieldMapping();
    lastPaymtDateMapping.setAttributeName("lastPaymtDate");
    lastPaymtDateMapping.setFieldName("PTRACCOUNT.LAST_PAYMT_DATE");
    descriptor.addMapping(lastPaymtDateMapping);

    DirectToFieldMapping lastTotalOutAmtMapping = new DirectToFieldMapping();
    lastTotalOutAmtMapping.setAttributeName("lastTotalOutAmt");
    lastTotalOutAmtMapping.setFieldName("PTRACCOUNT.LAST_TOTAL_OUT_AMT");
    descriptor.addMapping(lastTotalOutAmtMapping);

    DirectToFieldMapping membershipSinceMapping = new DirectToFieldMapping();
    membershipSinceMapping.setAttributeName("membershipSince");
    membershipSinceMapping.setFieldName("PTRACCOUNT.MEMBERSHIP_SINCE");
    descriptor.addMapping(membershipSinceMapping);

    DirectToFieldMapping outstandingAmtMapping = new DirectToFieldMapping();
    outstandingAmtMapping.setAttributeName("outstandingAmt");
    outstandingAmtMapping.setFieldName("PTRACCOUNT.OUTSTANDING_AMT");
    descriptor.addMapping(outstandingAmtMapping);

    DirectToFieldMapping overlimitIndicatorMapping = new DirectToFieldMapping();
    overlimitIndicatorMapping.setAttributeName("overlimitIndicator");
    overlimitIndicatorMapping.setFieldName("PTRACCOUNT.OVERLIMIT_IND");
    descriptor.addMapping(overlimitIndicatorMapping);

    DirectToFieldMapping parentAccountIdMapping = new DirectToFieldMapping();
    parentAccountIdMapping.setAttributeName("parentAccountId");
    parentAccountIdMapping.setFieldName("PTRACCOUNT.ACCOUNT_PARENT_ID");
    parentAccountIdMapping.readOnly();
    descriptor.addMapping(parentAccountIdMapping);

    DirectToFieldMapping principalOverdueMapping = new DirectToFieldMapping();
    principalOverdueMapping.setAttributeName("principalOverdue");
    principalOverdueMapping.setFieldName("PTRACCOUNT.PRINCIPAL_OVERDUE");
    descriptor.addMapping(principalOverdueMapping);

    DirectToFieldMapping profilingScoreMapping = new DirectToFieldMapping();
    profilingScoreMapping.setAttributeName("profilingScore");
    profilingScoreMapping.setFieldName("PTRACCOUNT.PROFILING_SCORE");
    descriptor.addMapping(profilingScoreMapping);

    DirectToFieldMapping provisionForBadDebtMapping = new DirectToFieldMapping();
    provisionForBadDebtMapping.setAttributeName("provisionForBadDebt");
    provisionForBadDebtMapping
        .setFieldName("PTRACCOUNT.PROVISION_FOR_BAD_DEBT");
    descriptor.addMapping(provisionForBadDebtMapping);

    DirectToFieldMapping remarksMapping = new DirectToFieldMapping();
    remarksMapping.setAttributeName("remarks");
    remarksMapping.setFieldName("PTRACCOUNT.REMARKS");
    descriptor.addMapping(remarksMapping);

    DirectToFieldMapping rescheduledMapping = new DirectToFieldMapping();
    rescheduledMapping.setAttributeName("rescheduled");
    rescheduledMapping.setFieldName("PTRACCOUNT.RESCHEDULED");
    descriptor.addMapping(rescheduledMapping);

    DirectToFieldMapping riskBehaviourScoreMapping = new DirectToFieldMapping();
    riskBehaviourScoreMapping.setAttributeName("riskBehaviourScore");
    riskBehaviourScoreMapping.setFieldName("PTRACCOUNT.RISK_BEHAVIOUR_SCORE");
    descriptor.addMapping(riskBehaviourScoreMapping);

    DirectToFieldMapping riskExternalScoreMapping = new DirectToFieldMapping();
    riskExternalScoreMapping.setAttributeName("riskExternalScore");
    riskExternalScoreMapping.setFieldName("PTRACCOUNT.RISK_EXTERNAL_SCORE");
    descriptor.addMapping(riskExternalScoreMapping);

    DirectToFieldMapping riskPaymentScoreMapping = new DirectToFieldMapping();
    riskPaymentScoreMapping.setAttributeName("riskPaymentScore");
    riskPaymentScoreMapping.setFieldName("PTRACCOUNT.RISK_PAYMENT_SCORE");
    descriptor.addMapping(riskPaymentScoreMapping);

    DirectToFieldMapping riskProfileScoreMapping = new DirectToFieldMapping();
    riskProfileScoreMapping.setAttributeName("riskProfileScore");
    riskProfileScoreMapping.setFieldName("PTRACCOUNT.RISK_PROFILE_SCORE");
    descriptor.addMapping(riskProfileScoreMapping);

    DirectToFieldMapping riskScoreMapping = new DirectToFieldMapping();
    riskScoreMapping.setAttributeName("riskScore");
    riskScoreMapping.setFieldName("PTRACCOUNT.RISK_SCORE");
    descriptor.addMapping(riskScoreMapping);

    DirectToFieldMapping stopStatementMapping = new DirectToFieldMapping();
    stopStatementMapping.setAttributeName("stopStatement");
    stopStatementMapping.setFieldName("PTRACCOUNT.STOP_STATEMENT");
    descriptor.addMapping(stopStatementMapping);

    DirectToFieldMapping terminationDateMapping = new DirectToFieldMapping();
    terminationDateMapping.setAttributeName("terminationDate");
    terminationDateMapping.setFieldName("PTRACCOUNT.TERMINATION_DATE");
    descriptor.addMapping(terminationDateMapping);

    DirectToFieldMapping totalDelqAmtMapping = new DirectToFieldMapping();
    totalDelqAmtMapping.setAttributeName("totalDelqAmt");
    totalDelqAmtMapping.setFieldName("PTRACCOUNT.TOTAL_DELQ_AMT");
    descriptor.addMapping(totalDelqAmtMapping);
    
    // Bucket mapping:
    // ALTER TABLE PTRACCOUNT ADD DELQ_AMT_X DECIMAL(14, 2);
    // ALTER TABLE PTRACCOUNT ADD DELQ_AMT_30 DECIMAL(14, 2);
    // ALTER TABLE PTRACCOUNT ADD DELQ_AMT_60 DECIMAL(14, 2);
    // ALTER TABLE PTRACCOUNT ADD DELQ_AMT_90 DECIMAL(14, 2);
    // ALTER TABLE PTRACCOUNT ADD DELQ_AMT_120 DECIMAL(14, 2);
    // ALTER TABLE PTRACCOUNT ADD DELQ_AMT_150 DECIMAL(14, 2);
    // ALTER TABLE PTRACCOUNT ADD DELQ_AMT_180 DECIMAL(14, 2);
    // ALTER TABLE PTRACCOUNT ADD DELQ_AMT_210 DECIMAL(14, 2);
    p.addDirectToFieldMapping("PTRACCOUNT.DELQ_AMT_X", "delinquentAmountX", descriptor);
    p.addDirectToFieldMapping("PTRACCOUNT.DELQ_AMT_30", "delinquentAmount30", descriptor);
    p.addDirectToFieldMapping("PTRACCOUNT.DELQ_AMT_60", "delinquentAmount60", descriptor);
    p.addDirectToFieldMapping("PTRACCOUNT.DELQ_AMT_90", "delinquentAmount90", descriptor);
    p.addDirectToFieldMapping("PTRACCOUNT.DELQ_AMT_120", "delinquentAmount120", descriptor);
    p.addDirectToFieldMapping("PTRACCOUNT.DELQ_AMT_150", "delinquentAmount150", descriptor);
    p.addDirectToFieldMapping("PTRACCOUNT.DELQ_AMT_180", "delinquentAmount180", descriptor);
    p.addDirectToFieldMapping("PTRACCOUNT.DELQ_AMT_210", "delinquentAmount210", descriptor);
    
    p.addDirectToFieldMapping("PTRACCOUNT.DELINQUENCY_COUNT_XDAYS", "delinquencyCountXDays", descriptor);
    p.addDirectToFieldMapping("PTRACCOUNT.DELINQUENCY_COUNT_30DAYS", "delinquencyCount30Days", descriptor);
    p.addDirectToFieldMapping("PTRACCOUNT.DELINQUENCY_COUNT_60DAYS", "delinquencyCount60Days", descriptor);
    p.addDirectToFieldMapping("PTRACCOUNT.DELINQUENCY_COUNT_90DAYS", "delinquencyCount90Days", descriptor);
    p.addDirectToFieldMapping("PTRACCOUNT.DELINQUENCY_COUNT_120DAYS", "delinquencyCount120Days", descriptor);
    p.addDirectToFieldMapping("PTRACCOUNT.DELINQUENCY_COUNT_150DAYS", "delinquencyCount150Days", descriptor);
    p.addDirectToFieldMapping("PTRACCOUNT.DELINQUENCY_COUNT_180DAYS", "delinquencyCount180Days", descriptor);
    p.addDirectToFieldMapping("PTRACCOUNT.DELINQUENCY_COUNT_210DAYS", "delinquencyCount210Days", descriptor);
    p.addDirectToFieldMapping("PTRACCOUNT.USER_CODE_3_ID", "userCode3Id", descriptor);
    
    DirectToFieldMapping treatmentStageStartDateMapping = new DirectToFieldMapping();
    treatmentStageStartDateMapping.setAttributeName("treatmentStageStartDate");
    treatmentStageStartDateMapping
        .setFieldName("PTRACCOUNT.TREATMENT_STAGE_START_DATE");
    descriptor.addMapping(treatmentStageStartDateMapping);

    DirectToFieldMapping weightedReceivableAmountMapping = new DirectToFieldMapping();
    weightedReceivableAmountMapping
        .setAttributeName("weightedReceivableAmount");
    weightedReceivableAmountMapping
        .setFieldName("PTRACCOUNT.WEIGHTED_RECEIVABLE_AMT");
    descriptor.addMapping(weightedReceivableAmountMapping);

    DirectToFieldMapping xDaysIndicatorMapping = new DirectToFieldMapping();
    xDaysIndicatorMapping.setAttributeName("xDaysIndicator");
    xDaysIndicatorMapping.setFieldName("PTRACCOUNT.X_DAYS_INDICATOR");
    descriptor.addMapping(xDaysIndicatorMapping);

    DirectToFieldMapping oldestOverdueDateMapping = new DirectToFieldMapping();
    oldestOverdueDateMapping.setAttributeName("oldestOverdueDate");
    oldestOverdueDateMapping.setFieldName("PTRACCOUNT.OLDEST_OVERDUE_DATE");
    descriptor.addMapping(oldestOverdueDateMapping);

    DirectToFieldMapping transferOrganizationNumberMapping = new DirectToFieldMapping();
    transferOrganizationNumberMapping.setAttributeName("transferOrganizationNumber");
    transferOrganizationNumberMapping.setFieldName("PTRACCOUNT.TRANSFER_ORGANIZATION_NUMBER");
    descriptor.addMapping(transferOrganizationNumberMapping);   
    
    DirectToFieldMapping transferTypeNumberMapping = new DirectToFieldMapping();
    transferTypeNumberMapping.setAttributeName("transferTypeNumber");
    transferTypeNumberMapping.setFieldName("PTRACCOUNT.TRANSFER_TYPE_NUMBER");
    descriptor.addMapping(transferTypeNumberMapping);
    
    DirectToFieldMapping transferAccountNumberMapping = new DirectToFieldMapping();
    transferAccountNumberMapping.setAttributeName("transferAccountNumber");
    transferAccountNumberMapping.setFieldName("PTRACCOUNT.TRANSFER_ACCOUNT_NUMBER");
    descriptor.addMapping(transferAccountNumberMapping);
    
    DirectToFieldMapping transferEffectiveDateMapping = new DirectToFieldMapping();
    transferEffectiveDateMapping.setAttributeName("transferEffectiveDate");
    transferEffectiveDateMapping.setFieldName("PTRACCOUNT.TRANSFER_EFFECTIVE_DATE");
    descriptor.addMapping(transferEffectiveDateMapping);
    
    DirectToFieldMapping fixedDepositPledgeFlagMapping = new DirectToFieldMapping();
    fixedDepositPledgeFlagMapping.setAttributeName("fixedDepositPledgeFlag");
    fixedDepositPledgeFlagMapping.setFieldName("PTRACCOUNT.FD_PLEDGE");
    descriptor.addMapping(fixedDepositPledgeFlagMapping);    
    
    DirectToFieldMapping legalCodeMapping = new DirectToFieldMapping();
    legalCodeMapping.setAttributeName("legalCode");
    legalCodeMapping.setFieldName("PTRACCOUNT.LEGAL_CODE");
    descriptor.addMapping(legalCodeMapping);
    
    DirectToFieldMapping dateBlockCodeMapping = new DirectToFieldMapping();
    dateBlockCodeMapping.setAttributeName("dateBlockCode");
    dateBlockCodeMapping.setFieldName("PTRACCOUNT.DATE_BLOCK_CODE");
    descriptor.addMapping(dateBlockCodeMapping);
    
    DirectToFieldMapping dateAlternateBlockCodeMapping = new DirectToFieldMapping();
    dateAlternateBlockCodeMapping.setAttributeName("dateAlternateBlockCode");
    dateAlternateBlockCodeMapping.setFieldName("PTRACCOUNT.DATE_ALTERNATE_BLOCK_CODE");
    descriptor.addMapping(dateAlternateBlockCodeMapping);    

    DirectToFieldMapping cashChargeOffMapping = new DirectToFieldMapping();
    cashChargeOffMapping.setAttributeName("cashChargeOff");
    cashChargeOffMapping.setFieldName("PTRACCOUNT.CASH_CHARGE_OFF");
    descriptor.addMapping(cashChargeOffMapping);       

    DirectToFieldMapping retailChargeOffMapping = new DirectToFieldMapping();
    retailChargeOffMapping.setAttributeName("retailChargeOff");
    retailChargeOffMapping.setFieldName("PTRACCOUNT.RETAIL_CHARGE_OFF");
    descriptor.addMapping(retailChargeOffMapping);    

    DirectToFieldMapping dateChargeOffMapping = new DirectToFieldMapping();
    dateChargeOffMapping.setAttributeName("dateChargeOff");
    dateChargeOffMapping.setFieldName("PTRACCOUNT.DATE_CHARGE_OFF");
    descriptor.addMapping(dateChargeOffMapping);            

    DirectToFieldMapping cardLinkIDMapping = new DirectToFieldMapping();
    cardLinkIDMapping.setAttributeName("cardLinkID");
    cardLinkIDMapping.setFieldName("PTRACCOUNT.HOST_USER_ID");
    cardLinkIDMapping.setNullValue("");    
    descriptor.addMapping(cardLinkIDMapping);
    
    OneToOneMapping agentBankMapping = new OneToOneMapping();
    agentBankMapping.setAttributeName("agentBankRef");
    agentBankMapping.setReferenceClass(com.profitera.descriptor.db.reference.AgentBankRef.class);
    agentBankMapping.useBasicIndirection();
    agentBankMapping.addForeignKeyFieldName("PTRACCOUNT.AGENT_BANK_REF","PTRAGENT_BANK_REF.ID");
    descriptor.addMapping(agentBankMapping);      

    OneToOneMapping accountRelationshipRefMapping = new OneToOneMapping();
    accountRelationshipRefMapping.setAttributeName("accountRelationshipRef");
    accountRelationshipRefMapping.setReferenceClass(com.profitera.descriptor.db.reference.AccountRelationshipRef.class);
    accountRelationshipRefMapping.useBasicIndirection();
    accountRelationshipRefMapping.addForeignKeyFieldName(
    "PTRACCOUNT.RELATIONSHIP_TYPE_ID","PTRACCOUNT_RELATIONSHIP_TYPE_REF.ID");
    descriptor.addMapping(accountRelationshipRefMapping);

    OneToOneMapping accountOwnerDetMapping = new OneToOneMapping();
    accountOwnerDetMapping.setAttributeName("accountOwnerDet");
    accountOwnerDetMapping
        .setReferenceClass(com.profitera.descriptor.db.account.AccountOwnerDetails.class);
    accountOwnerDetMapping.useBasicIndirection();
    accountOwnerDetMapping.readOnly();
    accountOwnerDetMapping.addForeignKeyFieldName("PTRACCOUNT.ACCOUNT_ID",
        "PTRACCOUNT_OWNER_DET.ACCOUNT_ID");
    descriptor.addMapping(accountOwnerDetMapping);

    OneToOneMapping accountStatusRefMapping = new OneToOneMapping();
    accountStatusRefMapping.setAttributeName("accountStatusRef");
    accountStatusRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.AccountStatusRef.class);
    accountStatusRefMapping.useBasicIndirection();
    accountStatusRefMapping.addForeignKeyFieldName(
        "PTRACCOUNT.ACCOUNT_STATUS_ID", "PTRACC_STATUS_REF.ACCOUNT_STATUS_ID");
    descriptor.addMapping(accountStatusRefMapping);

    OneToOneMapping accountWorklistStatusRefMapping = new OneToOneMapping();
    accountWorklistStatusRefMapping
        .setAttributeName("accountWorklistStatusRef");
    accountWorklistStatusRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.AccountWorkListStatusRef.class);
    accountWorklistStatusRefMapping.useBasicIndirection();
    accountWorklistStatusRefMapping.addForeignKeyFieldName(
        "PTRACCOUNT.ACC_WORKLIST_STATUS_ID",
        "PTRACC_WRKLIS_STATUS_REF.ACC_WRKLIST_STATUS_ID");
    descriptor.addMapping(accountWorklistStatusRefMapping);

    OneToOneMapping autoPayRefMapping = new OneToOneMapping();
    autoPayRefMapping.setAttributeName("autoPayRef");
    autoPayRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.AutoPayRef.class);
    autoPayRefMapping.useBasicIndirection();
    autoPayRefMapping.addForeignKeyFieldName("PTRACCOUNT.AUTO_PAY_IND",
        "PTRAUTO_PAY_REF.AUTO_PAY_ID");
    descriptor.addMapping(autoPayRefMapping);

    OneToOneMapping billingCycleRefMapping = new OneToOneMapping();
    billingCycleRefMapping.setAttributeName("billingCycleRef");
    billingCycleRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.BillingCycleRef.class);
    billingCycleRefMapping.useBasicIndirection();
    billingCycleRefMapping.addForeignKeyFieldName(
        "PTRACCOUNT.BILLING_CYCLE_ID", "PTRBILLING_CYCLE_REF.BILLING_CYCLE_ID");
    descriptor.addMapping(billingCycleRefMapping);

    OneToOneMapping blockCodeRefMapping = new OneToOneMapping();
    blockCodeRefMapping.setAttributeName("blockCodeRef");
    blockCodeRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.BlockCodeRef.class);
    blockCodeRefMapping.useBasicIndirection();
    blockCodeRefMapping.addForeignKeyFieldName("PTRACCOUNT.BLOCK_CODE_ID",
        "PTRBLOCK_CODE_REF.BLOCK_CODE_ID");
    descriptor.addMapping(blockCodeRefMapping);

    OneToOneMapping businessUnitMapping = new OneToOneMapping();
    businessUnitMapping.setAttributeName("businessUnit");
    businessUnitMapping
        .setReferenceClass(com.profitera.descriptor.db.user.BusinessUnit.class);
    businessUnitMapping.useBasicIndirection();
    businessUnitMapping.addForeignKeyFieldName("PTRACCOUNT.BRANCH_ID",
        "PTRBUSINESS_UNIT.BRANCH_ID");
    descriptor.addMapping(businessUnitMapping);

    OneToOneMapping campaignCodeRefMapping = new OneToOneMapping();
    campaignCodeRefMapping.setAttributeName("campaignCodeRef");
    campaignCodeRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.CampaignCodeRef.class);
    campaignCodeRefMapping.useBasicIndirection();
    campaignCodeRefMapping.addForeignKeyFieldName(
        "PTRACCOUNT.CAMPAIGN_CODE_ID", "PTRCAMPAIGN_CODE_REF.CAMPAIGN_CODE_ID");
    descriptor.addMapping(campaignCodeRefMapping);

    OneToOneMapping channelCodeRefMapping = new OneToOneMapping();
    channelCodeRefMapping.setAttributeName("channelCodeRef");
    channelCodeRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.ChannelCodeRef.class);
    channelCodeRefMapping.useBasicIndirection();
    channelCodeRefMapping.addForeignKeyFieldName("PTRACCOUNT.CHANNEL_CODE_ID",
        "PTRCHANNEL_CODE_REF.CHANNEL_CODE_ID");
    descriptor.addMapping(channelCodeRefMapping);

    OneToOneMapping chargeoffReasonRefMapping = new OneToOneMapping();
    chargeoffReasonRefMapping.setAttributeName("chargeoffReasonRef");
    chargeoffReasonRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.ChargeOffReasonRef.class);
    chargeoffReasonRefMapping.useBasicIndirection();
    chargeoffReasonRefMapping.addForeignKeyFieldName(
        "PTRACCOUNT.CHARGEOFF_REASON_ID",
        "PTRCHARGEOFF_REASON_REF.CHARGEOFF_REASON_ID");
    descriptor.addMapping(chargeoffReasonRefMapping);

    OneToOneMapping chargeoffStatusRefMapping = new OneToOneMapping();
    chargeoffStatusRefMapping.setAttributeName("chargeoffStatusRef");
    chargeoffStatusRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.ChargeOffStatusRef.class);
    chargeoffStatusRefMapping.useBasicIndirection();
    chargeoffStatusRefMapping.addForeignKeyFieldName(
        "PTRACCOUNT.CHARGEOFF_STATUS_ID",
        "PTRCHARGEOFF_STATUS_REF.CHARGEOFF_STATUS_ID");
    descriptor.addMapping(chargeoffStatusRefMapping);

    OneToOneMapping clientMapping = new OneToOneMapping();
    clientMapping.setAttributeName("client");
    clientMapping
        .setReferenceClass(com.profitera.descriptor.db.client.Client.class);
    clientMapping.useBasicIndirection();
    clientMapping.addForeignKeyFieldName("PTRACCOUNT.CLIENT_ID",
        "PTRCLIENT.CLIENT_ID");
    descriptor.addMapping(clientMapping);

    OneToOneMapping collectabilityStatusRefMapping = new OneToOneMapping();
    collectabilityStatusRefMapping.setAttributeName("collectabilityStatusRef");
    collectabilityStatusRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.CollectionStatusRef.class);
    collectabilityStatusRefMapping.useBasicIndirection();
    collectabilityStatusRefMapping
        .addForeignKeyFieldName("PTRACCOUNT.COLLECTIBILITY_STATUS",
            "PTRCOLL_STATUS_REF.COLL_STATUS_ID");
    descriptor.addMapping(collectabilityStatusRefMapping);

    OneToOneMapping collectionReasonRefMapping = new OneToOneMapping();
    collectionReasonRefMapping.setAttributeName("collectionReasonRef");
    collectionReasonRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.CollectionReasonRef.class);
    collectionReasonRefMapping.useBasicIndirection();
    collectionReasonRefMapping.addForeignKeyFieldName(
        "PTRACCOUNT.REASON_IN_COLLECTION",
        "PTRCOLLECTION_REASON_REF.COLLECTION_REASON_ID");
    descriptor.addMapping(collectionReasonRefMapping);

    OneToOneMapping currentResponsibleUserMapping = new OneToOneMapping();
    currentResponsibleUserMapping.setAttributeName("currentResponsibleUser");
    currentResponsibleUserMapping
        .setReferenceClass(com.profitera.descriptor.db.user.User.class);
    currentResponsibleUserMapping.useBasicIndirection();
    currentResponsibleUserMapping.addForeignKeyFieldName("PTRACCOUNT.USER_ID",
        "PTRUSER.USER_ID");
    descriptor.addMapping(currentResponsibleUserMapping);

    OneToOneMapping customerMapping = new OneToOneMapping();
    customerMapping.setAttributeName("customer");
    customerMapping
        .setReferenceClass(com.profitera.descriptor.db.account.Customer.class);
    customerMapping.useBasicIndirection();
    customerMapping.addForeignKeyFieldName("PTRACCOUNT.CUSTOMER_ID",
        "PTRCUSTOMER.CUSTOMER_ID");
    descriptor.addMapping(customerMapping);

    OneToOneMapping customerSegmentMapping = new OneToOneMapping();
    customerSegmentMapping.setAttributeName("customerSegment");
    customerSegmentMapping
        .setReferenceClass(com.profitera.descriptor.db.account.CustomerSegment.class);
    customerSegmentMapping.useBasicIndirection();
    customerSegmentMapping.addForeignKeyFieldName("PTRACCOUNT.CUST_SEGMENT_ID",
        "PTRCUSTOMER_SEGMENT.CUST_SEGMENT_ID");
    descriptor.addMapping(customerSegmentMapping);

    OneToOneMapping debtRecoveryStatusRefMapping = new OneToOneMapping();
    debtRecoveryStatusRefMapping.setAttributeName("debtRecoveryStatusRef");
    debtRecoveryStatusRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.DebtRecoveryStatusRef.class);
    debtRecoveryStatusRefMapping.useBasicIndirection();
    debtRecoveryStatusRefMapping.addForeignKeyFieldName(
        "PTRACCOUNT.DEBT_RECOVERY_STATUS",
        "PTRDEBTREC_STATUS_REF.DEBTREC_STATUS_ID");
    descriptor.addMapping(debtRecoveryStatusRefMapping);

    OneToOneMapping delinquencyTypeRefMapping = new OneToOneMapping();
    delinquencyTypeRefMapping.setAttributeName("delinquencyTypeRef");
    delinquencyTypeRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.DelinquencyTypeRef.class);
    delinquencyTypeRefMapping.useBasicIndirection();
    delinquencyTypeRefMapping.addForeignKeyFieldName(
        "PTRACCOUNT.DELINQUENCY_TYPE_ID",
        "PTRDELINQUENCY_TYPE_REF.DELINQUENCY_TYPE_ID");
    descriptor.addMapping(delinquencyTypeRefMapping);

    OneToOneMapping lastTreatmentActionMapping = new OneToOneMapping();
    lastTreatmentActionMapping.setAttributeName("lastTreatmentAction");
    lastTreatmentActionMapping
        .setReferenceClass(com.profitera.descriptor.db.treatment.TreatmentProcess.class);
    lastTreatmentActionMapping.useBasicIndirection();
    lastTreatmentActionMapping.addForeignKeyFieldName(
        "PTRACCOUNT.LAST_TREATMENT_ACTION",
        "PTRTREATMENT_PROCESS.TREATMENT_PROCESS_ID");
    descriptor.addMapping(lastTreatmentActionMapping);

    OneToOneMapping lastTreatmentResultMapping = new OneToOneMapping();
    lastTreatmentResultMapping.setAttributeName("lastTreatmentResult");
    lastTreatmentResultMapping
        .setReferenceClass(com.profitera.descriptor.db.treatment.TreatmentProcess.class);
    lastTreatmentResultMapping.useBasicIndirection();
    lastTreatmentResultMapping.addForeignKeyFieldName(
        "PTRACCOUNT.LAST_TREATMENT_RESULT",
        "PTRTREATMENT_PROCESS.TREATMENT_PROCESS_ID");
    descriptor.addMapping(lastTreatmentResultMapping);

    OneToOneMapping parentAccountMapping = new OneToOneMapping();
    parentAccountMapping.setAttributeName("parentAccount");
    parentAccountMapping
        .setReferenceClass(com.profitera.descriptor.db.account.Account.class);
    parentAccountMapping.useBasicIndirection();
    parentAccountMapping.addForeignKeyFieldName("PTRACCOUNT.ACCOUNT_PARENT_ID",
        "PTRACCOUNT.ACCOUNT_ID");
    descriptor.addMapping(parentAccountMapping);

    OneToOneMapping paymentBehaviourRefMapping = new OneToOneMapping();
    paymentBehaviourRefMapping.setAttributeName("paymentBehaviourRef");
    paymentBehaviourRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.PaymentBehaviourRef.class);
    paymentBehaviourRefMapping.useBasicIndirection();
    paymentBehaviourRefMapping.addForeignKeyFieldName(
        "PTRACCOUNT.PAYMENT_BEHAVIOUR_ID",
        "PTRPAYMENT_BEHAVIOUR_REF.BEHAVIOUR_ID");
    descriptor.addMapping(paymentBehaviourRefMapping);

    OneToOneMapping paymentFrequencyRefMapping = new OneToOneMapping();
    paymentFrequencyRefMapping.setAttributeName("paymentFrequencyRef");
    paymentFrequencyRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.PaymentFrequencyRef.class);
    paymentFrequencyRefMapping.useBasicIndirection();
    paymentFrequencyRefMapping.addForeignKeyFieldName(
        "PTRACCOUNT.PAYMENT_FREQUENCY_ID",
        "PTRPAYMENT_FREQUENCY_REF.PAYMENT_FREQUENCY_ID");
    descriptor.addMapping(paymentFrequencyRefMapping);

    OneToOneMapping productTypeRefMapping = new OneToOneMapping();
    productTypeRefMapping.setAttributeName("productTypeRef");
    productTypeRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.ProductTypeRef.class);
    productTypeRefMapping.useBasicIndirection();
    productTypeRefMapping.addForeignKeyFieldName("PTRACCOUNT.PRODUCT_TYPE_ID",
        "PTRPRODUCT_TYPE_REF.PRODUCT_TYPE_ID");
    descriptor.addMapping(productTypeRefMapping);

    OneToOneMapping profileSegmentRefMapping = new OneToOneMapping();
    profileSegmentRefMapping.setAttributeName("profileSegmentRef");
    profileSegmentRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.ProfileSegmentRef.class);
    profileSegmentRefMapping.useBasicIndirection();
    profileSegmentRefMapping.addForeignKeyFieldName(
        "PTRACCOUNT.PROFILE_SEGMENT_ID", "PTRPROFILE_SEGMENT_REF.PROFILE_ID");
    descriptor.addMapping(profileSegmentRefMapping);

    OneToOneMapping riskLevelRefMapping = new OneToOneMapping();
    riskLevelRefMapping.setAttributeName("riskLevelRef");
    riskLevelRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.RiskLevelRef.class);
    riskLevelRefMapping.useBasicIndirection();
    riskLevelRefMapping.addForeignKeyFieldName("PTRACCOUNT.RISK_LEVEL_ID",
        "PTRRISK_LEVEL_REF.RISK_LEVEL_ID");
    descriptor.addMapping(riskLevelRefMapping);

    OneToOneMapping sensitivityStatusRefMapping = new OneToOneMapping();
    sensitivityStatusRefMapping.setAttributeName("sensitivityStatusRef");
    sensitivityStatusRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.SensitiveStatusRef.class);
    sensitivityStatusRefMapping.useBasicIndirection();
    sensitivityStatusRefMapping.addForeignKeyFieldName(
        "PTRACCOUNT.SENSITIVITY_STATUS_ID",
        "PTRSENSITIVE_STATUS_REF.SENSE_STATUS_ID");
    descriptor.addMapping(sensitivityStatusRefMapping);

    OneToOneMapping treatmentStageRefMapping = new OneToOneMapping();
    treatmentStageRefMapping.setAttributeName("treatmentStageRef");
    treatmentStageRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.TreatmentStageRef.class);
    treatmentStageRefMapping.useBasicIndirection();
    treatmentStageRefMapping.addForeignKeyFieldName(
        "PTRACCOUNT.DELINQUENT_STAGE_ID",
        "PTRTREATMENT_STAGE_REF.TREATMENT_STAGE_ID");
    descriptor.addMapping(treatmentStageRefMapping);

    OneToOneMapping treatmentStreamRefMapping = new OneToOneMapping();
    treatmentStreamRefMapping.setAttributeName("treatmentStreamRef");
    treatmentStreamRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.TreatmentStreamRef.class);
    treatmentStreamRefMapping.useBasicIndirection();
    treatmentStreamRefMapping.addForeignKeyFieldName(
        "PTRACCOUNT.TREATMENT_STREAM_ID",
        "PTRTREATMENT_STREAM_REF.TREATMENT_STREAM_ID");
    descriptor.addMapping(treatmentStreamRefMapping);

    OneToOneMapping worklistMapping = new OneToOneMapping();
    worklistMapping.setAttributeName("worklist");
    worklistMapping
        .setReferenceClass(com.profitera.descriptor.db.worklist.WorkList.class);
    worklistMapping.useBasicIndirection();
    worklistMapping.addForeignKeyFieldName("PTRACCOUNT.WORK_LIST_ID",
        "PTRWORK_LIST.WORK_LIST_ID");
    descriptor.addMapping(worklistMapping);

    OneToManyMapping accountUnbilledMapping = new OneToManyMapping();
    accountUnbilledMapping.setAttributeName("accountUnbilled");
    accountUnbilledMapping
        .setReferenceClass(com.profitera.descriptor.db.account.AccountUnbilled.class);
    accountUnbilledMapping.useBasicIndirection();
    accountUnbilledMapping.addTargetForeignKeyFieldName(
        "PTRACCOUNT_UNBILLED.ACCOUNT_ID", "PTRACCOUNT.ACCOUNT_ID");
    descriptor.addMapping(accountUnbilledMapping);

    OneToManyMapping accountWorklistHistoryMapping = new OneToManyMapping();
    accountWorklistHistoryMapping.setAttributeName("accountWorklistHistory");
    accountWorklistHistoryMapping
        .setReferenceClass(com.profitera.descriptor.db.history.AccountWorkListHistory.class);
    accountWorklistHistoryMapping.useBasicIndirection();
    accountWorklistHistoryMapping.addTargetForeignKeyFieldName(
        "PTRACCOUNT_WORKLIST_HISTORY.ACCOUNT_ID", "PTRACCOUNT.ACCOUNT_ID");
    descriptor.addMapping(accountWorklistHistoryMapping);

    OneToManyMapping childAccountsMapping = new OneToManyMapping();
    childAccountsMapping.setAttributeName("childAccounts");
    childAccountsMapping
        .setReferenceClass(com.profitera.descriptor.db.account.Account.class);
    childAccountsMapping.useBasicIndirection();
    childAccountsMapping.addTargetForeignKeyFieldName(
        "PTRACCOUNT.ACCOUNT_PARENT_ID", "PTRACCOUNT.ACCOUNT_ID");
    descriptor.addMapping(childAccountsMapping);

    OneToManyMapping paymentsMapping = new OneToManyMapping();
    paymentsMapping.setAttributeName("payments");
    paymentsMapping
        .setReferenceClass(com.profitera.descriptor.db.payment.Payment.class);
    paymentsMapping.useBasicIndirection();
    paymentsMapping.addTargetForeignKeyFieldName("PTRPAYMENT.ACCOUNT_ID",
        "PTRACCOUNT.ACCOUNT_ID");
    descriptor.addMapping(paymentsMapping);

    OneToManyMapping treatmentPlansMapping = new OneToManyMapping();
    treatmentPlansMapping.setAttributeName("treatmentPlans");
    treatmentPlansMapping
        .setReferenceClass(com.profitera.descriptor.db.account.AccountTreatmentPlan.class);
    treatmentPlansMapping.useBasicIndirection();
    treatmentPlansMapping.addTargetForeignKeyFieldName(
        "PTRACC_TREATMENT_PLAN.ACCOUNT_ID", "PTRACCOUNT.ACCOUNT_ID");
    descriptor.addMapping(treatmentPlansMapping);

    ManyToManyMapping contactsMapping = new ManyToManyMapping();
    contactsMapping.setAttributeName("contacts");
    contactsMapping
        .setReferenceClass(com.profitera.descriptor.db.contact.AddressDetails.class);
    contactsMapping.useBasicIndirection();
    contactsMapping.setRelationTableName("PTRACCOUNT_CONTACT_REL");
    contactsMapping.addSourceRelationKeyFieldName(
        "PTRACCOUNT_CONTACT_REL.ACCOUNT_ID", "PTRACCOUNT.ACCOUNT_ID");
    contactsMapping.addTargetRelationKeyFieldName(
        "PTRACCOUNT_CONTACT_REL.CONTACT_ID", "PTRADDRESS_DET.CONTACT_ID");
    descriptor.addMapping(contactsMapping);

    DirectToFieldMapping transferDateMapping = new DirectToFieldMapping();
    transferDateMapping.setAttributeName("transferDate");
    transferDateMapping.setFieldName("PTRACCOUNT.TRANSFER_DATE");
    descriptor.addMapping(transferDateMapping);

    OneToOneMapping transferredAccountMapping = new OneToOneMapping();
    transferredAccountMapping.setAttributeName("transferredFromAccount");
    transferredAccountMapping
        .setReferenceClass(com.profitera.descriptor.db.account.Account.class);
    transferredAccountMapping.useBasicIndirection();
    transferredAccountMapping.addForeignKeyFieldName("PTRACCOUNT.TRANSFERRED_FROM_ACCOUNT_ID",
        "PTRACCOUNT.ACCOUNT_ID");
    descriptor.addMapping(transferredAccountMapping);

    return descriptor;
  }
}
