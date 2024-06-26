package mk.ukim.finki.localfix.service;

import mk.ukim.finki.localfix.model.Problem_Administrator;
import mk.ukim.finki.localfix.model.User;
import mk.ukim.finki.localfix.model.enums.Status;

import java.util.List;

public interface ProblemAdministratorService {

    List<Problem_Administrator> listAllProblemAdministrators();

    void deleteProblemAdministrator(Long id);

    Problem_Administrator create(Problem_Administrator problemAdministrator);

    List<Problem_Administrator> listAllProblemAdministratorsByCityIdAndStatus(Long cityId, Status status, User user);
}
