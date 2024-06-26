package mk.ukim.finki.localfix.repository;

import mk.ukim.finki.localfix.model.Problem;
import mk.ukim.finki.localfix.model.User;
import mk.ukim.finki.localfix.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemRepository extends JpaRepository<Problem,Long> {

    List<Problem> findAllByCityIdAndStatusAndReportedBy(Long cityId, Status status, User user);

    List<Problem> findAllByReportedBy(User user);

    List<Problem> findAllByCityId(Long cityId);

    List<Problem> findAllByStatus(Status status);

    List<Problem> findAllByCityIdAndStatus(Long cityId, Status status);

    List<Problem> findAllByCityIdAndReportedBy(Long cityId, User user);

    List<Problem> findAllByStatusAndReportedBy(Status status, User user);

    void deleteAllByReportedBy(User user);
}
