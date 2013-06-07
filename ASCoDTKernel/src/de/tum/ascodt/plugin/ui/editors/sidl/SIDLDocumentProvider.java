package de.tum.ascodt.plugin.ui.editors.sidl;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;

import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;



/**
 * This class creates the document and sets the document partitioner
 * @author Atanas Atanasov
 *
 */
public class SIDLDocumentProvider extends FileDocumentProvider{

	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if(document != null) {
			IDocumentPartitioner partitioner= new FastPartitioner(SIDLEditor.getDefault().getSIDLPartitionScanner(), SIDLPartitionScanner.SIDL_PARTITION_TYPES);

			IDocumentExtension3 extension= (IDocumentExtension3) document;
			extension.setDocumentPartitioner(SIDLEditor.SIDL_PARTITIONING, partitioner);



			partitioner.connect(document);

		}

		return document;
	}

}
