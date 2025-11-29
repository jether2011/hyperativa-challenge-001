package br.com.hyperativa.service.domain.processor;

import br.com.hyperativa.service.domain.exceptions.FileUploadException;

public interface Processor <I, O> {
    O process(I input) throws FileUploadException;
}
