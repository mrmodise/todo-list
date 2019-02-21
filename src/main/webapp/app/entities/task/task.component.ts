import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { ITask } from 'app/shared/model/task.model';
import { AccountService } from 'app/core';
import { TaskService } from './task.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
    selector: 'jhi-task',
    templateUrl: './task.component.html'
})
export class TaskComponent implements OnInit, OnDestroy {
    tasks: ITask[];
    currentAccount: any;
    eventSubscriber: Subscription;
    searchForm: FormGroup;

    constructor(
        protected taskService: TaskService,
        protected jhiAlertService: JhiAlertService,
        protected eventManager: JhiEventManager,
        protected accountService: AccountService,
        protected formBuilder: FormBuilder
    ) {}

    loadAll() {
        this.taskService
            .query()
            .pipe(
                filter((res: HttpResponse<ITask[]>) => res.ok),
                map((res: HttpResponse<ITask[]>) => res.body)
            )
            .subscribe(
                (res: ITask[]) => {
                    this.tasks = res;
                },
                (res: HttpErrorResponse) => this.onError(res.message)
            );
    }

    ngOnInit() {
        this.loadAll();
        this.accountService.identity().then(account => {
            this.currentAccount = account;
        });
        this.registerChangeInTasks();
        this.createSearchForm();
    }

    createSearchForm() {
        return (this.searchForm = this.formBuilder.group({
            term: ['', Validators.required]
        }));
    }

    search() {
        const term = this.searchForm.get('term').value;
        this.taskService
            .query(term)
            .pipe(
                filter((res: HttpResponse<ITask[]>) => res.ok),
                map((res: HttpResponse<ITask[]>) => res.body)
            )
            .subscribe(
                (res: ITask[]) => {
                    this.tasks = res;
                    console.log(`res ${JSON.stringify(res)}`);
                },
                (res: HttpErrorResponse) => this.onError(res.message)
            );
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
    }

    trackId(index: number, item: ITask) {
        return item.id;
    }

    registerChangeInTasks() {
        this.eventSubscriber = this.eventManager.subscribe('taskListModification', response => this.loadAll());
    }

    protected onError(errorMessage: string) {
        this.jhiAlertService.error(errorMessage, null, null);
    }
}
