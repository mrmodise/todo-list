import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: 'task',
                loadChildren: './task/task.module#TodoTaskModule'
            },
            {
                path: 'category',
                loadChildren: './category/category.module#TodoCategoryModule'
            },
            {
                path: 'activity',
                loadChildren: './activity/activity.module#TodoActivityModule'
            },
            {
                path: 'activity',
                loadChildren: './activity/activity.module#TodoActivityModule'
            }
            /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
        ])
    ],
    declarations: [],
    entryComponents: [],
    providers: [],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class TodoEntityModule {}
