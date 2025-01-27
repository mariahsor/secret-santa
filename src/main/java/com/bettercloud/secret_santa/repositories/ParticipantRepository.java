package com.bettercloud.secret_santa.repositories;

import com.bettercloud.secret_santa.entities.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Integer> {
    Optional<Participant> findByEmail(String email);
}
