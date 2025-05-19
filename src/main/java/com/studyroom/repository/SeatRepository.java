package com.studyroom.repository;

import org.springframework.data.jpa.repository.Query;
import java.util.List;
import com.studyroom.model.Room;
import com.studyroom.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    Optional<Seat> findByRoomAndSeatNumber(Room room, String seatNumber);
    List<Seat> findByRoomId(Long roomId);

    @Query("SELECT s FROM Seat s WHERE s.room.name LIKE %:query% OR s.seatNumber LIKE %:query%")
    List<Seat> searchSeats(String query);


    @Query("SELECT s FROM Seat s WHERE s.id = :seatId AND s.room.id = :roomId")
    Optional<Seat> findByRoomIdSeatId(@Param("roomId") Long roomId,@Param("seatId") Long seatId);

    @Query("SELECT s FROM Seat s WHERE s.id = :seatId")
    Seat findBySeatId(@Param("seatId") Long seatId);
}
