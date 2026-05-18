import {computed, Injectable, signal} from '@angular/core';
import {Learner, Person, Qualification} from "../../model/Maintain";

@Injectable({
	providedIn: 'root'
})
export class MaintainStateService {
	// Signaux partagés entre PersonService et QualificationService
	readonly selectedPersons = signal<Person[]>([]);
	readonly selectedQualifications = signal<Qualification[]>([]);
	readonly selectedLearners = signal<Learner[]>([]);

	// Signaux computed pour vérifier si les chargements sont en cours
	readonly isLoadingPersons = signal<boolean>(false);
	readonly isLoadingQualifications = signal<boolean>(false);
	readonly isLoadingLearners = signal<boolean>(false);

	// État du master actif
	readonly currentMaster = signal<'person' | 'qualification' | 'learner'>('person');
	readonly isPersonMaster = computed(() => this.currentMaster() === 'person');
	readonly isQualificationMaster = computed(() => this.currentMaster() === 'qualification');
	readonly isLearnerMaster = computed(() => this.currentMaster() === 'learner');

	// Signals pour indiquer si enregistrer sans plausibiliser
	registerWithoutPlausi = signal(false);

	setSelectedPersons(persons: Person[]): void {
		this.selectedPersons.set(persons);
	}

	setSelectedQualifications(qualifications: Qualification[]): void {
		this.selectedQualifications.set(qualifications);
	}

	setSelectedLearners(learners: Learner[]): void {
		this.selectedLearners.set(learners);
	}

	setLoadingPersons(isLoading: boolean): void {
		this.isLoadingPersons.set(isLoading);
	}

	setLoadingQualifications(isLoading: boolean): void {
		this.isLoadingQualifications.set(isLoading);
	}

	setLoadingLearners(isLoading: boolean): void {
		this.isLoadingLearners.set(isLoading);
	}

	setCurrentMaster(current: 'person' | 'qualification' | 'learner'): void {
		this.currentMaster.set(current);
	}
}
