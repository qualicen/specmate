package com.specmate.connectors.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.service.log.LogService;

import com.specmate.common.exception.SpecmateException;
import com.specmate.connectors.api.IRequirementsSource;
import com.specmate.connectors.internal.ConnectorTask;
import com.specmate.model.base.BaseFactory;
import com.specmate.model.base.Folder;
import com.specmate.model.base.IContainer;
import com.specmate.model.requirements.Requirement;
import com.specmate.model.requirements.RequirementsFactory;
import com.specmate.model.support.util.SpecmateEcoreUtil;
import com.specmate.persistency.IChange;
import com.specmate.persistency.ITransaction;

public class ConnectorTaskTest {

	private BasicEList<EObject> contentList;

	public ConnectorTaskTest() throws Exception {
		super();
	}

	@Before
	public void setUpContentList() {
		contentList = new BasicEList<EObject>();
	}

	@Test
	public void testRequirementsNumbers() throws SpecmateException {
		checkRequirementsNumber(0);
		checkRequirementsNumber(1);
		checkRequirementsNumber(ConnectorTask.BATCH_SIZE - 1);
		checkRequirementsNumber(ConnectorTask.BATCH_SIZE);
		checkRequirementsNumber(ConnectorTask.BATCH_SIZE + 1);
		checkRequirementsNumber(10000);
	}

	private void checkRequirementsNumber(int count) throws SpecmateException {
		IRequirementsSource reqSource = new TestRequirementSource_VariableNumbers(count);
		runConnectorTaskWithSource(reqSource);

		Folder folder = (Folder) contentList.get(0);
		assertEquals(reqSource.getId(), folder.getId());
		assertEquals(reqSource.getId(), folder.getId());
		assertEquals(reqSource.getId(), folder.getName());

		if (count == 0) {
			assertEquals(0, folder.getContents().size());
			return;
		}
		Folder subfolder = (Folder) folder.getContents().get(0);
		assertEquals(TestRequirementSource_InvalidRequirements.FOLDER_NAME, subfolder.getId());
		assertEquals(TestRequirementSource_InvalidRequirements.FOLDER_NAME, subfolder.getName());

		for (int i = 1; i <= count; i++) {
			Requirement req = (Requirement) SpecmateEcoreUtil.getEObjectWithId("id" + i, subfolder.getContents());
			assertEquals("id" + i, req.getId());
			assertEquals("req" + i, req.getName());
		}
	}

	@Test
	public void testWithoutFolder() throws SpecmateException {
		IRequirementsSource reqSource = new TestRequirementSource_NoFolder();
		runConnectorTaskWithSource(reqSource);

		// Project Folder exists
		Folder folder = (Folder) contentList.get(0);
		assertEquals(reqSource.getId(), folder.getId());
		assertEquals(reqSource.getId(), folder.getId());
		assertEquals(reqSource.getId(), folder.getName());

		assertEquals(1, folder.getContents().size());

		Requirement req = (Requirement) SpecmateEcoreUtil.getEObjectWithId("id1", folder.getContents());
		assertEquals("id1", req.getId());
		assertEquals("req1", req.getName());
	}

	@Test
	public void testNameChange() throws SpecmateException {
		IRequirementsSource reqSource = new TestRequirementSource_NameChange();
		runConnectorTaskWithSource(reqSource);

		Folder folder = (Folder) contentList.get(0);

		Folder subfolder = (Folder) folder.getContents().get(0);

		Requirement req = (Requirement) SpecmateEcoreUtil.getEObjectWithId("id1", subfolder.getContents());
		assertEquals("id1", req.getId());
		assertEquals("req1_1", req.getName());

		runConnectorTaskWithSource(reqSource);
		req = (Requirement) SpecmateEcoreUtil.getEObjectWithId("id1", subfolder.getContents());
		assertEquals("id1", req.getId());
		assertEquals("req1_2", req.getName());
	}

	@Test
	public void testParentChange() throws SpecmateException {
		IRequirementsSource reqSource = new TestRequirementSource_ParentChange();
		runConnectorTaskWithSource(reqSource);

		Folder folder = (Folder) contentList.get(0);

		Folder subfolder1 = (Folder) SpecmateEcoreUtil.getEObjectWithId("folder1", folder.getContents());

		Requirement req = (Requirement) SpecmateEcoreUtil.getEObjectWithId("id1", subfolder1.getContents());
		assertEquals("id1", req.getId());
		assertEquals("req1", req.getName());

		runConnectorTaskWithSource(reqSource);
		assertEquals(0, subfolder1.getContents().size());

		Folder subfolder2 = (Folder) SpecmateEcoreUtil.getEObjectWithId("folder2", folder.getContents());
		req = (Requirement) SpecmateEcoreUtil.getEObjectWithId("id1", subfolder2.getContents());
		assertEquals("id1", req.getId());
		assertEquals("req1", req.getName());
	}

	@Test
	public void testConnectorService() throws SpecmateException {

		IRequirementsSource reqSource = new TestRequirementSource_InvalidRequirements();
		runConnectorTaskWithSource(reqSource);

		Folder folder = (Folder) contentList.get(0);
		assertEquals(reqSource.getId(), folder.getId());

		assertEquals(reqSource.getId(), folder.getId());
		assertEquals(reqSource.getId(), folder.getName());

		Folder subfolder = (Folder) folder.getContents().get(0);
		assertEquals(TestRequirementSource_InvalidRequirements.FOLDER_NAME, subfolder.getId());
		assertEquals(TestRequirementSource_InvalidRequirements.FOLDER_NAME, subfolder.getName());

		Requirement req = (Requirement) SpecmateEcoreUtil.getEObjectWithId("id1", subfolder.getContents());
		assertEquals("id1", req.getId());
		assertEquals(TestRequirementSource_InvalidRequirements.REQ_NAME + "   ", req.getName());

		req = (Requirement) SpecmateEcoreUtil.getEObjectWithId("id2", subfolder.getContents());
		assertNotNull(req);
		assertEquals(req.getId(), req.getName());

		req = (Requirement) SpecmateEcoreUtil.getEObjectWithId("id3", subfolder.getContents());
		assertNotNull(req);
		assertEquals(req.getId(), req.getName());
	}

	private void runConnectorTaskWithSource(IRequirementsSource reqSource) throws SpecmateException {
		ITransaction transaction = mock(ITransaction.class);
		Resource resource = mock(Resource.class);
		when(resource.getContents()).thenReturn(contentList);
		when(transaction.getResource()).thenReturn(resource);
		when(transaction.doAndCommit(Mockito.any(IChange.class))).thenAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock inv) throws Throwable {
				IChange change = inv.getArgument(0);
				change.doChange();
				return null;
			}

		});

		ConnectorTask task = new ConnectorTask(Arrays.asList(reqSource), transaction, mock(LogService.class));
		task.run();
	}

	private abstract class TestRequirementSourceBase implements IRequirementsSource {
		public static final String REQ_NAME = "req";
		public static final String FOLDER_NAME = "folder";
		private Folder folder;

		@Override
		public String getId() {
			return "testSource";
		}

		@Override
		public IContainer getContainerForRequirement(Requirement requirement) throws SpecmateException {
			if (folder == null) {
				folder = BaseFactory.eINSTANCE.createFolder();
				folder.setId(FOLDER_NAME);
				folder.setName(FOLDER_NAME);
			}
			return folder;
		}

		@Override
		public boolean authenticate(String username, String password) throws SpecmateException {
			return true;
		}
	}

	private class TestRequirementSource_VariableNumbers extends TestRequirementSourceBase {

		private int count;

		public TestRequirementSource_VariableNumbers(int count) {
			super();
			this.count = count;
		}

		@Override
		public Collection<Requirement> getRequirements() throws SpecmateException {
			List<Requirement> requirements = new ArrayList<>();
			for (int i = 1; i <= count; i++) {
				Requirement req = RequirementsFactory.eINSTANCE.createRequirement();
				req.setName("req" + i);
				req.setId("id" + i);
				req.setExtId("id" + i);
				requirements.add(req);
			}
			return requirements;
		}
	}

	private class TestRequirementSource_NoFolder extends TestRequirementSourceBase {

		@Override
		public Collection<Requirement> getRequirements() throws SpecmateException {

			Requirement req = RequirementsFactory.eINSTANCE.createRequirement();
			req.setName("req1");
			req.setId("id1");
			req.setExtId("id1");
			return Arrays.asList(req);
		}

		@Override
		public IContainer getContainerForRequirement(Requirement requirement) throws SpecmateException {
			return null;
		}

	}

	private class TestRequirementSource_NameChange extends TestRequirementSourceBase {

		int requestCounter = 1;

		@Override
		public Collection<Requirement> getRequirements() throws SpecmateException {

			Requirement req = RequirementsFactory.eINSTANCE.createRequirement();
			req.setName("req1_" + requestCounter);
			req.setId("id1");
			req.setExtId("id1");
			requestCounter++;
			return Arrays.asList(req);
		}
	}

	private class TestRequirementSource_ParentChange extends TestRequirementSourceBase {

		int requestCounter = 1;

		@Override
		public Collection<Requirement> getRequirements() throws SpecmateException {

			Requirement req = RequirementsFactory.eINSTANCE.createRequirement();
			req.setName("req1");
			req.setId("id1");
			req.setExtId("id1");
			return Arrays.asList(req);
		}

		@Override
		public IContainer getContainerForRequirement(Requirement requirement) throws SpecmateException {
			Folder folder = BaseFactory.eINSTANCE.createFolder();
			folder = BaseFactory.eINSTANCE.createFolder();
			folder.setId("folder" + requestCounter);
			folder.setName("folder" + requestCounter);
			requestCounter++;
			return folder;
		}
	}

	private class TestRequirementSource_InvalidRequirements extends TestRequirementSourceBase {
		private static final String BAD_CHARS = ",|;";

		@Override
		public Collection<Requirement> getRequirements() throws SpecmateException {
			Requirement req1 = RequirementsFactory.eINSTANCE.createRequirement();
			req1.setName(REQ_NAME + BAD_CHARS);
			req1.setId("id1");
			req1.setExtId("id1");

			Requirement req2 = RequirementsFactory.eINSTANCE.createRequirement();
			req2.setName(null);
			req2.setId("id2");
			req2.setExtId("id2");

			Requirement req3 = RequirementsFactory.eINSTANCE.createRequirement();
			req3.setName("");
			req3.setId("id3");
			req3.setExtId("id3");

			return Arrays.asList(req1, req2, req3);
		}

	}

}
