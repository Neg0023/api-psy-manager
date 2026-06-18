package com.psymanager.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Verifica choque de horário: existe sessão (não cancelada, e diferente da que
     * está sendo editada) cujo intervalo se sobrepõe a [start, end)?
     * Sobreposição = início existente &lt; novo fim E fim existente &gt; novo início.
     */
    @Query("""
            select (count(a) > 0) from Appointment a
            where a.status not in :ignoredStatuses
              and a.startTime < :end
              and a.endTime > :start
              and (:excludeId is null or a.id <> :excludeId)
            """)
    boolean existsOverlapping(@Param("start") OffsetDateTime start,
                             @Param("end") OffsetDateTime end,
                             @Param("excludeId") Long excludeId,
                             @Param("ignoredStatuses") Collection<AppointmentStatus> ignoredStatuses);

    /** Sessões que se sobrepõem ao intervalo visível do calendário [rangeStart, rangeEnd]. */
    List<Appointment> findByStartTimeLessThanAndEndTimeGreaterThanOrderByStartTime(
            OffsetDateTime rangeEnd, OffsetDateTime rangeStart);
}
