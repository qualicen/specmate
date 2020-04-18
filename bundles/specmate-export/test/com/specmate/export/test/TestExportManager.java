package com.specmate.export.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.service.log.LogService;

import com.specmate.auth.api.ISessionListener;
import com.specmate.auth.api.ISessionService;
import com.specmate.common.exception.SpecmateException;
import com.specmate.export.api.IExporter;
import com.specmate.export.internal.services.ExportManagerService;
import com.specmate.model.testspecification.TestProcedure;
import com.specmate.model.testspecification.TestSpecification;
import com.specmate.model.testspecification.TestspecificationFactory;
import com.specmate.usermodel.UserSession;
import com.specmate.usermodel.UsermodelFactory;

public class TestExportManager {

	private static final String TOKEN = "testtoken";
	private ISessionListener listener;

	@Test
	public void testExportManager() throws SpecmateException {

		testExportManagerWithParameters(false, false, true, false);
		testExportManagerWithParameters(false, true, false, false);
		testExportManagerWithParameters(true, false, true, true);
		testExportManagerWithParameters(false, true, true, true);

	}

	private void testExportManagerWithParameters(boolean projectIsNull, boolean userAuthorizedForProject,
			boolean userAuthorizedForExporter, boolean exportAvailable) throws SpecmateException {
		ExportManagerService exportManagerService = new ExportManagerService();
		LogService logService = mock(LogService.class);
		exportManagerService.setLogService(logService);

		ISessionService sessionService = mock(ISessionService.class);
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) {
				ISessionListener givenListener = (ISessionListener) invocation.getArgument(0);
				listener = givenListener;
				return null;
			}
		}).when(sessionService).registerSessionListener(Mockito.any());
		Mockito.when(sessionService.isAuthorizedProject(any(UserSession.class), any()))
				.thenReturn(userAuthorizedForProject);

		IExporter exporter = Mockito.mock(IExporter.class);
		when(exporter.getProjectName()).thenReturn(projectIsNull ? null : "theproject");
		when(exporter.canExportTestProcedure()).thenReturn(true);
		when(exporter.canExportTestSpecification()).thenReturn(true);
		when(exporter.isAuthorizedToExport(any(), any())).thenReturn(userAuthorizedForExporter);
		exportManagerService.addTestSpecificationExporter(exporter);

		exportManagerService.setSessionService(sessionService);
		exportManagerService.activate();

		UserSession session = UsermodelFactory.eINSTANCE.createUserSession();
		session.setId(TOKEN);
		listener.sessionCreated(session, "irrelevantUser", "irrelevantPassword");

		TestSpecification ts = TestspecificationFactory.eINSTANCE.createTestSpecification();
		TestProcedure tp = TestspecificationFactory.eINSTANCE.createTestProcedure();

		if (exportAvailable) {
			Assert.assertEquals(1, exportManagerService.getExporters(ts, TOKEN).size());
			Assert.assertEquals(1, exportManagerService.getExporters(tp, TOKEN).size());
		} else {
			Assert.assertEquals(0, exportManagerService.getExporters(ts, TOKEN).size());
			Assert.assertEquals(0, exportManagerService.getExporters(tp, TOKEN).size());
		}
	}
}
