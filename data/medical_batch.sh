./run.sh kc:/medicine/disease:symptoms \
	/medicine/disease/symptoms \
	medicine_disease_symptoms

./run.sh kc:/medicine/medical_treatment:side\ effects \
	/medicine/medical_treatment/side_effects \
	treatment_side_effects 

./run.sh kc:/medicine/disease:causes \
	/medicine/disease_cause/diseases \
	disease_causes 

./run.sh kc:/medicine/disease:causes \
	/medicine/disease_cause/diseases \
	disease_causes2

rm disease_causes2-questions.csv
rm disease_causes2-gold.csv

./run.sh kc:/medicine/drug:side\ effects \
        /medicine/medical_treatment/side_effects \
        drug_side_effects

./run.sh kc:/medicine/disease:nondrug\ treatments \
        /medicine/disease/treatments \
        nondrug_treatment


