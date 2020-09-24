import { Component, OnInit } from '@angular/core';
import { LinkingDialogComponent } from '../../../linking-dialog/components/linking-dialog/linking-dialog.component';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { ModalService } from '../../../../../notification/modules/modals/services/modal-service';

@Component({
  selector: 'link-model-button',
  templateUrl: './link-model-button.component.html',
  styleUrls: ['./link-model-button.component.css']
})
export class LinkModelButtonComponent implements OnInit {

  private dialogOpen = false;
  private modalRef: NgbModalRef;

  constructor(private modalService: ModalService) {
  }

  ngOnInit(): void {
  }

  openLinkingDialog(): void {
    if (!this.dialogOpen) {
      this.dialogOpen = true;
      this.modalRef = this.modalService.open(LinkingDialogComponent, {
          // large size
          size: 'lg',
          // don't allow to hide the modal by clicking the background
          backdrop: 'static',
          // don't allow to hide the modal by hitting ESC
          keyboard: true,
          // center modal vertically
          centered: true
      });

      this.modalRef.result.then((link) => {
          this.dialogOpen = false;
          // The resulting Link
          console.log(link);
      }).catch(() => {
          this.dialogOpen = false;
      });
    }
  }
}
