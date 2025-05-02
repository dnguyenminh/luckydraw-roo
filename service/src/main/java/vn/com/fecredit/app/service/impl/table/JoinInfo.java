package vn.com.fecredit.app.service.impl.table;

import jakarta.persistence.criteria.JoinType;
import lombok.Getter;

import java.util.Objects;

/**
 * Information about a join between entities
 */
@Getter
public class JoinInfo {
    private final String alias;
    private final String propertyName;
    private final Class<?> sourceEntityClass;
    private final Class<?> targetEntityClass;
    private final JoinType joinType;
    
    public JoinInfo(String alias, String propertyName, Class<?> sourceEntityClass, 
                    Class<?> targetEntityClass, JoinType joinType) {
        this.alias = alias;
        this.propertyName = propertyName;
        this.sourceEntityClass = sourceEntityClass;
        this.targetEntityClass = targetEntityClass;
        this.joinType = joinType;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JoinInfo joinInfo = (JoinInfo) o;
        return Objects.equals(alias, joinInfo.alias);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(alias);
    }
}
