package org.scalaide.worksheet.reconciler

import org.eclipse.jface.text.DocumentEvent
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IDocumentListener
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.reconciler.DirtyRegion
import org.eclipse.jface.text.reconciler.IReconcilingStrategy
import org.scalaide.logging.HasLogger
import org.scalaide.worksheet.ScriptCompilationUnit
import org.scalaide.worksheet.editor.ScriptEditor

class ScalaReconcilingStrategy(textEditor: ScriptEditor) extends IReconcilingStrategy with HasLogger {
  private var document: IDocument = _
  private lazy val scriptUnit = ScriptCompilationUnit.fromEditor(textEditor)

  override def setDocument(doc: IDocument) {
    document = doc

    doc.addDocumentListener(reloader)
  }

  override def reconcile(dirtyRegion: DirtyRegion, subRegion: IRegion) {
    logger.debug("Incremental reconciliation not implemented.")
  }

  override def reconcile(partition: IRegion) {
    val errors = scriptUnit.reconcile(document.get)

    textEditor.updateErrorAnnotations(errors)
  }

  /** Ask the underlying unit to reload on each document change event.
   *
   *  This is certainly wasteful, but otherwise the AST trees are not up to date
   *  in the interval between the last keystroke and reconciliation (which has a delay of
   *  500ms usually). The user can be quick and ask for completions in this interval, and get
   *  wrong results.
   */
  private object reloader extends IDocumentListener {
    override def documentChanged(event: DocumentEvent) {
      scriptUnit.askReload()
    }

    override def documentAboutToBeChanged(event: DocumentEvent) {}
  }
}