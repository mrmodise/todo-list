import { NgModule } from '@angular/core';

import { TodoSharedLibsModule, JhiAlertComponent, JhiAlertErrorComponent } from './';

@NgModule({
    imports: [TodoSharedLibsModule],
    declarations: [JhiAlertComponent, JhiAlertErrorComponent],
    exports: [TodoSharedLibsModule, JhiAlertComponent, JhiAlertErrorComponent]
})
export class TodoSharedCommonModule {}
