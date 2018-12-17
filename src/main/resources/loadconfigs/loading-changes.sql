alter table PTRPAYMENT_ACKNOWLEDGMENT_IMPORT_RECORD add PERMANENT_ACCOUNT_NO TEXT;

CREATE TABLE PTRAGENCY_WORK_LIST_ASSIGNMENT ( 
	AGENCY_ASSIGNMENT_ID	BIGINT NOT NULL,
	ACCOUNT_ID          	BIGINT NOT NULL,
	WORK_LIST_ID        	BIGINT,
	ASSIGNMENT_DATE     	DATE NOT NULL,
	USER_ID             	TEXT,
	PRIMARY KEY(AGENCY_ASSIGNMENT_ID)
);
	INVALID         	BOOL,
alter table ptragency_effective_assignment add column total_os decimal(19,2);
PRIMARY KEY (AGENCY_ASSIGNMENT_ID,FIELD_ID,ID)
CREATE SEQUENCE PTRLEGAL_ACTION_PROCESS_ADDITIONAL_DETAIL_ID;
	   IS_DEFAULT BOOL NOT NULL DEFAULT FALSE,
alter table ptrgenerated_billing add column approved_user_id text;

--29.12.2017--
create table ptrwaps_contact_history (
id bigserial,
account_id bigint,
contact_type_id bigint,
spouse_id_number text,
spouse_name text,
address_1 text,
address_2 text,
address_3 text,
city text,
zip_code text,
state_id bigint,
load_execution_id bigint,
primary key (id)
);




 