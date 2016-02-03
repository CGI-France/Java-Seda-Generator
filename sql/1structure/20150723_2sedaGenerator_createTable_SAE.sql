CREATE TABLE SAE (
	SAE_AccordVersement text NOT NULL,
	SAE_Serveur text NOT NULL,
	TransferIdPrefix text NULL,
	TransferIdValue text NULL,
	SAE_ProfilArchivage text NULL,
	TransferringAgencyId text NULL,
	TransferringAgencyName text NULL,
	TransferringAgencyDesc text NULL,
	ArchivalAgencyId text NULL,
	ArchivalAgencyName text NULL,
	ArchivalAgencyDesc text NULL
);

ALTER TABLE SAE ADD PRIMARY KEY(SAE_AccordVersement, SAE_Serveur);
