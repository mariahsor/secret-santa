package com.bettercloud.secret_santa.repositories;

import com.bettercloud.secret_santa.entities.LogAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LogAssignmentRepository extends JpaRepository<LogAssignment, Integer> {

    /**
     * Returns how many times a giver has assigned a gift to a receiver
     * in the last 3 years (including the provided limit year).
     */
    @Query(value = """
        SELECT COUNT(*)
        FROM log_assignments
        WHERE giver_id = :giverId
          AND receiver_id = :receiverId
          AND year >= :yearLimit
        """,
            nativeQuery = true)
    int countRecentAssignments(
            @Param("giverId") Integer giverId,
            @Param("receiverId") Integer receiverId,
            @Param("yearLimit") Integer yearLimit
    );
}
