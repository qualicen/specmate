package com.specmate.persistency.hibernate.internal;

import java.util.Map;

import org.eclipse.net4j.util.event.IEvent;
import org.eclipse.net4j.util.event.IListener;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;

import com.specmate.common.exception.SpecmateException;
import com.specmate.persistency.IPersistencyService;
import com.specmate.persistency.ITransaction;
import com.specmate.persistency.IView;
import com.specmate.persistency.cdo.internal.CDOPersistencyServiceConfig;

@Component(service = IPersistencyService.class, configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = CDOPersistencyServiceConfig.PID)
public class HibernatePersistencyService implements IPersistencyService, IListener {

	private SessionFactory sessionFactory;

	@Activate
	public void activate(Map<String, Object> properties) throws SpecmateException {
		start();
	}

	@Deactivate
	public void deactivate() {
		this.shutdown();
	}

	@Override
	public ITransaction openTransaction() throws SpecmateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITransaction openTransaction(boolean attachCommitListeners) throws SpecmateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IView openView() throws SpecmateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() throws SpecmateException {
		// TODO Auto-generated method stub
		setUp();
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.saveOrUpdate(new PersistedClass(1, "My Class", true));
		session.getTransaction().commit();
		session.close();
	}

	@Override
	public void notifyEvent(IEvent event) {
		// TODO Auto-generated method stub

	}

	protected void setUp() {
		// A SessionFactory is set up once for an application!
		final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure() // configures settings
																									// from
																									// hibernate.cfg.xml
				.build();
		try {
			sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
		} catch (Exception e) {
			// The registry would be destroyed by the SessionFactory, but we had trouble
			// building the SessionFactory
			// so destroy it manually.
			StandardServiceRegistryBuilder.destroy(registry);
		}
	}
}
