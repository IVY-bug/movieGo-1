package com.movieGo.entity;

import lombok.Data;
import lombok.Getter;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
@Data
public abstract class baseEntity implements Comparable<baseEntity>, Serializable {

    /**
	 * 反序列化时校验版本
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private @Getter Long id;

    @Column(nullable = false)
    private @Getter Date createdAt;

    @Column(nullable = false)
    private @Getter Date updatedAt;

    @PrePersist
    public void prePersist(){
        createdAt = updatedAt = new Date();
    }

    @PreUpdate
    public void preUpdate(){
        updatedAt = new Date();
    }

    @Override
    public int compareTo(baseEntity o) {
        return this.getId().compareTo(o.getId());
    }
    public int hashCode() {
        return new HashCodeBuilder().append(getId()).toHashCode();
    }
    public boolean equals(Object other) {
        if (other == null || other.getClass() != this.getClass())
            return false;

        return this.getId().equals(((baseEntity) other).getId());
    }
}