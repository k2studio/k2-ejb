package in.k2s.ejb;

import in.k2s.core.bundle.K2Bundle;
import in.k2s.core.interfaces.Entity;
import in.k2s.jpa.dao.BaseDAO;
import in.k2s.jpa.sequence.SequenceGenerator;
import in.k2s.util.data.DataUtil;
import in.k2s.web.profile.Profile;

import java.sql.Timestamp;

import net.vidageek.mirror.dsl.Mirror;

public abstract class BaseEJBImpl implements BaseEJB {
	
	private final Mirror mirror = new Mirror();
	protected BaseDAO dao;
	
	protected BaseDAO getBaseDAO() {
		return dao;
	}
	
	protected Mirror getMirror() {
		return this.mirror;
	}
	
	protected <T extends Entity>T save(Entity entity) {
		return this.save(entity, null);
	}
	
	protected <T extends Entity>T save(Entity entity, Profile profile) {
		if(K2Bundle.getBoolean("jpa.generate.id")) entity.setId(generateId(profile));
		if(K2Bundle.getBoolean("jpa.generate.chave")) getMirror().on(entity).set().field("chave").withValue(SequenceGenerator.generateUUID());
		if(K2Bundle.getBoolean("jpa.auditoria")) entity = auditoria(entity, profile);
		entity = getBaseDAO().insert(entity);
		return (T) entity;
	}
	
	protected <T extends Entity>T update(Entity entity) {
		return this.update(entity, null);
	}
	
	protected <T extends Entity>T update(Entity entity, Profile profile) {
		if(K2Bundle.getBoolean("jpa.auditoria")) entity = auditoria(entity, profile);
		Entity newEntity = getBaseDAO().update(entity);
		return (T) newEntity;
	}
	
	protected <T extends Entity>T delete(Entity entity) {
		entity = getBaseDAO().remove(entity);
		return (T) entity;
	}
	
	protected <T extends Entity>T auditoria(Entity object, Profile profile) {
		Long userID = 0L;
		if(profile != null && profile.getId() == null) userID = profile.getId();
		Timestamp time = DataUtil.getTimestamp();
		
		if(getMirror().on(object).get().field("insertTime") == null) getMirror().on(object).set().field("insertTime").withValue(time);
		if(getMirror().on(object).get().field("insertBy")   == null) getMirror().on(object).set().field("insertBy").withValue(userID);
		
		getMirror().on(object).set().field("updateTime").withValue(time);
		getMirror().on(object).set().field("updateBy").withValue(userID);
		
		return (T) object;
	}
	
	private Long generateId(Profile profile) {
		Long id;
		if(profile == null) id = SequenceGenerator.generate();
		else id = SequenceGenerator.generate(profile.getParentId());
		return id;
	}
	
}