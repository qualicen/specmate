package com.specmate.testspecification.internal.services;

import java.util.Optional;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.eclipse.emf.ecore.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.specmate.common.exception.SpecmateException;
import com.specmate.config.api.IConfigService;
import com.specmate.emfrest.api.IRestService;
import com.specmate.emfrest.api.RestServiceBase;
import com.specmate.export.api.IExporter;
import com.specmate.model.export.Export;
import com.specmate.model.requirements.CEGModel;
import com.specmate.model.requirements.RequirementsFactory;
import com.specmate.model.testspecification.TestSpecification;
import com.specmate.model.testspecification.TestspecificationFactory;
import com.specmate.modelgeneration.GenerateModelFromRequirementService;
import com.specmate.nlp.api.ELanguage;
import com.specmate.nlp.api.INLPService;
import com.specmate.rest.RestResult;
import com.specmate.testspecification.internal.generators.CEGTestCaseGenerator;

@Component(service = IRestService.class)
public class OneTimeTestGeneratorService extends RestServiceBase {

	private INLPService nlpService;
	private IConfigService configService;
	private IExporter csvExporter;
	private GenerateModelFromRequirementService cegGenerator;

	@Override
	public String getServiceName() {
		return "generateTestsOnce";
	}

	@Override
	public boolean canGet(Object object) {
		return object instanceof Resource;
	}

	@Override
	public RestResult<?> get(Object object, MultivaluedMap<String, String> queryParams, String token)
			throws SpecmateException {
		String text = queryParams.getFirst("text");
		String lang = queryParams.getFirst("lang").toLowerCase();
		ELanguage langEnum = lang.equals("de") ? ELanguage.DE : ELanguage.EN;
		CEGModel model = RequirementsFactory.eINSTANCE.createCEGModel();
		model.setModelRequirements(text);
		model = cegGenerator.generateModelFromDescription(model);
		TestSpecification spec = TestspecificationFactory.eINSTANCE.createTestSpecification();
		spec.setName("generated");
		model.getContents().add(spec);
		CEGTestCaseGenerator testGenerator = new CEGTestCaseGenerator(spec);
		testGenerator.generate();
		Optional<Export> export = csvExporter.export(spec);
		return new RestResult<String>(Response.Status.OK, export.get().getContent());
	}

	@Reference(target = "(type=csv)")
	public void setCSVExporter(IExporter exporter) {
		csvExporter = exporter;
	}

	@Reference
	public void setNLPService(INLPService nlpService) {
		this.nlpService = nlpService;
	}

	@Reference
	public void setConfigService(IConfigService configService) {
		this.configService = configService;
	}

	@Reference
	public void setCEGGenerator(GenerateModelFromRequirementService cegGenerator) {
		this.cegGenerator = cegGenerator;
	}
}
