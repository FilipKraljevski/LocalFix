package mk.ukim.finki.localfix.repository;

import mk.ukim.finki.localfix.model.Problem_Administrator;
import mk.ukim.finki.localfix.model.User;
import mk.ukim.finki.localfix.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemAdministratorRepository extends JpaRepository<Problem_Administrator, Long> {

    List<Problem_Administrator> findAllByAdministratorId(Long administratorId);

    void deleteByProblemId (Long problemId);

    Problem_Administrator findByProblemId(Long problemId);

    List<Problem_Administrator> findAllByProblem_City_IdAndProblem_Status(Long cityId, Status status);

    List<Problem_Administrator> findAllByProblem_City_IdAndProblem_StatusAndProblem_ReportedBy(Long cityId, Status status, User user);

    List<Problem_Administrator> findAllByProblem_City_IdAndProblem_ReportedBy(Long cityId, User user);

    List<Problem_Administrator> findAllByProblem_StatusAndProblem_ReportedBy(Status status, User user);

    List<Problem_Administrator> findAllByProblem_City_Id(Long cityId);

    List<Problem_Administrator> findAllByProblem_Status(Status status);

    List<Problem_Administrator> findAllByProblem_ReportedBy(User reportedBy);


}
