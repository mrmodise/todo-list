import { Moment } from 'moment';
import { ICategory } from 'app/shared/model/category.model';

export interface IActivity {
    id?: number;
    title?: string;
    description?: string;
    dueDate?: Moment;
    completed?: boolean;
    category?: string;
    categories?: ICategory;
}

export class Activity implements IActivity {
    constructor(
        public id?: number,
        public title?: string,
        public description?: string,
        public dueDate?: Moment,
        public completed?: boolean,
        public category?: string,
        public categories?: ICategory
    ) {
        this.completed = this.completed || false;
    }
}
