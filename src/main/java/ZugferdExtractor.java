import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.common.PDNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;

import java.io.*;
import java.util.Map;

public class ZugferdExtractor {

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.out.println("Usage: java -jar zugferd-extractor.jar <input.pdf> <output.xml>");
            System.exit(1);
        }

        File pdfFile = new File(args[0]);
        File xmlFile = new File(args[1]);

        try (PDDocument document = PDDocument.load(pdfFile)) {

            PDDocumentNameDictionary names =
                    document.getDocumentCatalog().getNames();

            if (names == null || names.getEmbeddedFiles() == null) {
                throw new RuntimeException("No embedded files found");
            }

            PDNameTreeNode<PDComplexFileSpecification> embeddedFiles =
                    names.getEmbeddedFiles();

            Map<String, PDComplexFileSpecification> fileMap =
                    embeddedFiles.getNames();

            if (fileMap == null) {
                throw new RuntimeException("No embedded file map found");
            }

            for (Map.Entry<String, PDComplexFileSpecification> entry : fileMap.entrySet()) {

                String filename = entry.getKey().toLowerCase();

                if (filename.endsWith(".xml")) {

                    PDComplexFileSpecification fileSpec = entry.getValue();
                    PDEmbeddedFile embeddedFile = fileSpec.getEmbeddedFile();

                    try (InputStream is = embeddedFile.createInputStream();
                         OutputStream os = new FileOutputStream(xmlFile)) {

                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = is.read(buffer)) != -1) {
                            os.write(buffer, 0, read);
                        }
                    }

                    System.out.println("Extracted embedded XML: " + filename);
                    return;
                }
            }

            throw new RuntimeException("No ZUGFeRD XML found inside PDF");
        }
    }
}
