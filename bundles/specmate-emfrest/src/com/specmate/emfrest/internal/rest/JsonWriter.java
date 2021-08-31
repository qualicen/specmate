package com.specmate.emfrest.internal.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.emf.ecore.EObject;
import org.osgi.service.log.Logger;

import com.specmate.common.ISerializationConfiguration;
import com.specmate.emfjson.EMFJsonSerializer;
import com.specmate.urihandler.IURIFactory;

/** Serializes EMF object to JSON */
public class JsonWriter {

	Logger logger;

	public static final String MEDIA_TYPE = MediaType.APPLICATION_JSON + ";charset=utf-8";

	private EMFJsonSerializer serializer;

	/**
	 * constructor
	 * 
	 * @param logger
	 */
	public JsonWriter(Logger logger, IURIFactory factory, ISerializationConfiguration serializationConfig) {
		this.serializer = new EMFJsonSerializer(factory, serializationConfig);
		this.logger = logger;
	}

	/** {@inheritDoc} */
	public long getSize(Object obj, Class<?> clazz, Type type, Annotation[] annotation, MediaType mediaType) {
		return -1;
	}

	/** {@inheritDoc} */
	public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotation, MediaType mediaType) {
		return mediaType.toString().equals(MEDIA_TYPE) && (EObject.class.isAssignableFrom(clazz)
				|| List.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException
	 */
	public void writeTo(Object obj, Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> headers, OutputStream stream) throws IOException {

		String result = null;
		if (obj instanceof EObject) {
			try {
				result = serializer.serialize((EObject) obj).toString();
			} catch (Exception e) {
				logger.error("Could not serialize object.", e);
				throw new WebApplicationException(e);
			}
		} else if (obj instanceof List) {
			try {
				result = serializer.serialize((List<?>) obj).toString();
			} catch (Exception e) {
				logger.error("Could not serialize object.", e);
				throw new WebApplicationException(e);
			}
		} else if (obj instanceof Map) {
			try {
				result = serializer.serialize((Map<String, String>) obj).toString();
			} catch (Exception e) {
				logger.error("Could not serialize object.", e);
				throw new WebApplicationException(e);
			}
		} else {
			throw new WebApplicationException("Cannot serialize " + clazz);
		}

		OutputStreamWriter writer = new OutputStreamWriter(stream, "utf-8");
		writer.write(result);
		writer.flush();
	}
}
