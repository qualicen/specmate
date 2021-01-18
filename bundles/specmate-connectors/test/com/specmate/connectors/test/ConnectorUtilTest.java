package com.specmate.connectors.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.specmate.connectors.api.ConnectorBase;
import com.specmate.connectors.api.IConnector;
import com.specmate.connectors.api.IProject;
import com.specmate.connectors.api.IProjectService;
import com.specmate.connectors.internal.ConnectorUtil;
import com.specmate.model.base.BaseFactory;
import com.specmate.model.base.Folder;
import com.specmate.model.base.IContainer;
import com.specmate.model.requirements.Requirement;
import com.specmate.model.requirements.RequirementsFactory;
import com.specmate.model.support.util.SpecmateEcoreUtil;
import com.specmate.persistency.IChange;
import com.specmate.persistency.ITransaction;

public class ConnectorUtilTest {

	private BasicEList<EObject> contentList;

	public ConnectorUtilTest() throws Exception {
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
		checkRequirementsNumber(ConnectorUtil.BATCH_SIZE - 1);
		checkRequirementsNumber(ConnectorUtil.BATCH_SIZE);
		checkRequirementsNumber(ConnectorUtil.BATCH_SIZE + 1);
		checkRequirementsNumber(10000);
	}

	private void checkRequirementsNumber(int count) throws SpecmateException {
		IConnector reqSource = new TestRequirementSource_VariableNumbers(count);
		runConnectorUtilWithSourceFullSync(reqSource);

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
		IConnector reqSource = new TestRequirementSource_NoFolder();
		runConnectorUtilWithSourceFullSync(reqSource);

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
	public void testSingleSync() throws SpecmateException {
		IConnector reqSource = new TestRequirementSource_SingleRequirement();
		String id = "singleId";
		runConnectorUtilWithSourceSingleSync(id, reqSource);

		Folder folder = (Folder) contentList.get(0);
		assertEquals(reqSource.getId(), folder.getId());
		assertEquals(reqSource.getId(), folder.getId());
		assertEquals(reqSource.getId(), folder.getName());

		assertEquals(1, folder.getContents().size());

		Folder subfolder = (Folder) folder.getContents().get(0);
		assertEquals(TestRequirementSource_InvalidRequirements.FOLDER_NAME, subfolder.getId());
		assertEquals(TestRequirementSource_InvalidRequirements.FOLDER_NAME, subfolder.getName());

		Requirement req = (Requirement) SpecmateEcoreUtil.getEObjectWithId(id, subfolder.getContents());
		assertEquals(id, req.getId());
		assertEquals("req", req.getName());
	}

	@Test
	public void testNameChange() throws SpecmateException {
		IConnector reqSource = new TestRequirementSource_NameChange();
		runConnectorUtilWithSourceFullSync(reqSource);

		Folder folder = (Folder) contentList.get(0);

		Folder subfolder = (Folder) folder.getContents().get(0);

		Requirement req = (Requirement) SpecmateEcoreUtil.getEObjectWithId("id1", subfolder.getContents());
		assertEquals("id1", req.getId());
		assertEquals("req1_1", req.getName());

		runConnectorUtilWithSourceFullSync(reqSource);
		req = (Requirement) SpecmateEcoreUtil.getEObjectWithId("id1", subfolder.getContents());
		assertEquals("id1", req.getId());
		assertEquals("req1_2", req.getName());
	}

	@Test
	public void testParentChange() throws SpecmateException {
		IConnector reqSource = new TestRequirementSource_ParentChange();
		runConnectorUtilWithSourceFullSync(reqSource);

		Folder folder = (Folder) contentList.get(0);

		Folder subfolder1 = (Folder) SpecmateEcoreUtil.getEObjectWithId("folder1", folder.getContents());

		Requirement req = (Requirement) SpecmateEcoreUtil.getEObjectWithId("id1", subfolder1.getContents());
		assertEquals("id1", req.getId());
		assertEquals("req1", req.getName());

		runConnectorUtilWithSourceFullSync(reqSource);
		assertEquals(0, subfolder1.getContents().size());

		Folder subfolder2 = (Folder) SpecmateEcoreUtil.getEObjectWithId("folder2", folder.getContents());
		req = (Requirement) SpecmateEcoreUtil.getEObjectWithId("id1", subfolder2.getContents());
		assertEquals("id1", req.getId());
		assertEquals("req1", req.getName());
	}

	@Test
	public void testConnectorService() throws SpecmateException {

		IConnector reqSource = new TestRequirementSource_InvalidRequirements();
		runConnectorUtilWithSourceFullSync(reqSource);

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

	private void runConnectorUtilWithSourceFullSync(IConnector reqSource) throws SpecmateException {
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
		ConnectorUtil.syncRequirementsFromSources(Arrays.asList(reqSource), transaction, mock(LogService.class));
	}

	private void runConnectorUtilWithSourceSingleSync(String id, IConnector reqSource) throws SpecmateException {
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
		ConnectorUtil.syncRequirementById(id, reqSource, transaction, mock(LogService.class));
	}

	private abstract class TestRequirementSourceBase extends ConnectorBase {
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
		public Set<IProject> authenticate(String username, String password, IProject logonProject,
				IProjectService projectService) throws SpecmateException {
			return new HashSet<IProject>(Arrays.asList(logonProject));
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

		@Override
		public Requirement getRequirementById(String id) throws SpecmateException {
			return null;
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

		@Override
		public Requirement getRequirementById(String id) throws SpecmateException {
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

		@Override
		public Requirement getRequirementById(String id) throws SpecmateException {
			return null;
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

		@Override
		public Requirement getRequirementById(String id) throws SpecmateException {
			return null;
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

		@Override
		public Requirement getRequirementById(String id) throws SpecmateException {
			return null;
		}
	}

	private class TestRequirementSource_SingleRequirement extends TestRequirementSourceBase {

		@Override
		public Collection<Requirement> getRequirements() throws SpecmateException {
			return null;
		}

		@Override
		public Requirement getRequirementById(String id) throws SpecmateException {
			Requirement req = RequirementsFactory.eINSTANCE.createRequirement();
			req.setName("req");
			req.setId(id);
			req.setExtId(id);
			return req;
		}
	}
}
