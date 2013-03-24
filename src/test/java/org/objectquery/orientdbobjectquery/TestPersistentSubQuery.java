package org.objectquery.orientdbobjectquery;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectquery.ObjectQuery;
import org.objectquery.generic.GenericObjectQuery;
import org.objectquery.orientdbobjectquery.domain.Dog;
import org.objectquery.orientdbobjectquery.domain.Person;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public class TestPersistentSubQuery {

	private OObjectDatabaseTx db;

	@Before
	public void beforeTest() {
		db = OrientDBTestHelper.getPool().acquire();
		db.begin();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSubquerySimple() {
		ObjectQuery<Person> query = new GenericObjectQuery<Person>(Person.class);

		ObjectQuery<Person> subQuery = query.subQuery(Person.class);
		subQuery.eq(subQuery.target().getName(), "tomdud");
		query.eq(query.target().getDud(), subQuery);

		List<Person> res = (List<Person>) OrientDBObjectQuery.execute(query, db);
		Assert.assertEquals(1, res.size());
		Assert.assertEquals(res.get(0).getName(), "tom");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testBackReferenceSubquery() {
		GenericObjectQuery<Person> query = new GenericObjectQuery<Person>(Person.class);
		Person target = query.target();
		ObjectQuery<Person> subQuery = query.subQuery(Person.class);
		subQuery.eq(subQuery.target().getDog().getName(), target.getDog().getName());
		subQuery.notEq(subQuery.target(), target);
		query.eq(query.target().getDud(), subQuery);

		List<Person> res = (List<Person>) OrientDBObjectQuery.execute(query, db);
		Assert.assertEquals(1, res.size());
		Assert.assertEquals(res.get(0).getName(), "tom");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDoubleSubQuery() {

		GenericObjectQuery<Person> query = new GenericObjectQuery<Person>(Person.class);
		Person target = query.target();
		ObjectQuery<Person> subQuery = query.subQuery(Person.class);
		query.eq(target.getDud(), subQuery);
		subQuery.eq(subQuery.target().getDog().getName(), target.getDog().getName());
		ObjectQuery<Dog> doubSubQuery = subQuery.subQuery(Dog.class);
		subQuery.eq(subQuery.target().getDog(), doubSubQuery);

		doubSubQuery.notEq(doubSubQuery.target().getOwner(), subQuery.target());
		doubSubQuery.notEq(doubSubQuery.target().getOwner(), query.target().getMum());

		List<Person> res = (List<Person>) OrientDBObjectQuery.execute(query, db);
		Assert.assertEquals(1, res.size());
		Assert.assertEquals(res.get(0).getName(), "tom");

	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMultipleReferenceSubquery() {
		GenericObjectQuery<Person> query = new GenericObjectQuery<Person>(Person.class);
		Person target = query.target();
		ObjectQuery<Person> subQuery = query.subQuery(Person.class);
		subQuery.eq(subQuery.target().getName(), "tomdud");
		ObjectQuery<Person> subQuery1 = query.subQuery(Person.class);
		subQuery1.eq(subQuery1.target().getName(), "tommum");
		query.eq(target.getDud(), subQuery);
		query.eq(target.getMum(), subQuery1);

		List<Person> res = (List<Person>) OrientDBObjectQuery.execute(query, db);
		Assert.assertEquals(1, res.size());
		Assert.assertEquals(res.get(0).getName(), "tom");

	}

}