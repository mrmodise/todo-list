import { Moment } from 'moment';
import { ICategory } from 'app/shared/model/category.model';

export interface ITask {
    id?: number;
    title?: string;
    description?: string;
    dueDate?: Moment;
    completed?: boolean;
    category?: ICategory;
}

export class Task implements ITask {
    constructor(
        public id?: number,
        public title?: string,
        public description?: string,
        public dueDate?: Moment,
        public completed?: boolean,
        public category?: ICategory
    ) {
        this.completed = this.completed || false;
    }
}
