package com.specmate.emfrest.internal.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import org.osgi.service.log.Logger;

import com.specmate.common.ISerializationConfiguration;
import com.specmate.urihandler.IURIFactory;

public class JSONMapWriter implements MessageBodyWriter<Map<String, String>> {

	/** The wrapped JsonWriter */
	private JsonWriter writer;

	/** constructor */
	public JSONMapWriter(@Context Logger logger, @Context IURIFactory factory,
			@Context ISerializationConfiguration serializationConfig) {
		this.writer = new JsonWriter(logger, factory, serializationConfig);
	}

	/** {@inheritDoc} */
	@Override
	public long getSize(Map<String, String> obj, Class<?> clazz, Type type, Annotation[] annotation,
			MediaType mediaType) {
		return writer.getSize(obj, clazz, type, annotation, mediaType);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotation, MediaType mediaType) {
		return writer.isWriteable(clazz, type, annotation, mediaType);
	}

	/** {@inheritDoc} */
	@Override
	public void writeTo(Map<String, String> obj, Class<?> clazz, Type type, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> headers, OutputStream stream)
			throws IOException, WebApplicationException {
		writer.writeTo(obj, clazz, type, annotations, mediaType, headers, stream);
	}
}
