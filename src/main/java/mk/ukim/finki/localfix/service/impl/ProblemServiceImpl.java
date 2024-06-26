package mk.ukim.finki.localfix.service.impl;


import javax.transaction.Transactional;
import mk.ukim.finki.localfix.model.City;
import mk.ukim.finki.localfix.model.Institution;
import mk.ukim.finki.localfix.model.Problem;
import mk.ukim.finki.localfix.model.User;
import mk.ukim.finki.localfix.model.enums.Impact;
import mk.ukim.finki.localfix.model.enums.Status;
import mk.ukim.finki.localfix.model.exceptions.CityNotFoundException;
import mk.ukim.finki.localfix.model.exceptions.InstitutionNotFoundException;
import mk.ukim.finki.localfix.model.exceptions.ProblemNotFoundException;
import mk.ukim.finki.localfix.model.exceptions.UserNotFoundException;
import mk.ukim.finki.localfix.repository.*;
import mk.ukim.finki.localfix.service.ProblemService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository problemRepository;
    private final InstitutionRepository institutionRepository;

    private final UserRepository userRepository;

    private final CityRepository cityRepository;


    public ProblemServiceImpl(ProblemRepository problemRepository,
                              InstitutionRepository institutionRepository,
                              UserRepository userRepository,
                              CityRepository cityRepository
                              ) {
        this.problemRepository = problemRepository;
        this.institutionRepository = institutionRepository;
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;

    }


    @Override
    public List<Problem> listAllProblems() {
        return this.problemRepository.findAll();
    }


    @Override
    public Problem findProblemById(Long id) {

        return this.problemRepository.findById(id).orElseThrow(() ->
                new ProblemNotFoundException(id));
    }

    @Override
    public Optional<Problem> saveProblem(String title, String description, byte[] photo,
                                         Long institutionId, Long cityId,Impact impact,String address, Long userId
                                         ) {

        User reportedBy = this.userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException(userId));

        Institution institution = this.institutionRepository.findById(institutionId).orElseThrow(() ->
                new InstitutionNotFoundException(institutionId));

        City city = this.cityRepository.findById(cityId).orElseThrow(() ->
                new CityNotFoundException(cityId));

        Problem problem = new Problem(title,description,photo,institution,reportedBy,impact,city,address);

        return Optional.of(this.problemRepository.save(problem));
    }

    @Transactional
    @Override
    public Optional<Problem> editProblem(Long id, String title,
                                         String address,
                                         byte[] photo,
                                         String description, Status status,
                                         Impact impact,
                                         Long institutionId,
                                         Long cityId) {

        Institution institution = this.institutionRepository.findById(institutionId).orElseThrow(() ->
                new InstitutionNotFoundException(institutionId));
        City city = this.cityRepository.findById(cityId).orElseThrow(() ->
                new CityNotFoundException(cityId));



        Problem problem = this.findProblemById(id);
        problem.setTitle(title);
        problem.setAddress(address);
        problem.setDescription(description);

        problem.setStatus(status);
        problem.setImpact(impact);

        problem.setInstitution(institution);
        problem.setCity(city);

        return Optional.of(this.problemRepository.save(problem));

    }



    @Override
    public void deleteProblemById(Long id) {
        this.problemRepository.deleteById(id);
    }

    @Transactional
    @Override
    public List<Problem> listAllProblemsByCityIdAndStatus(Long id,Status status, User user) {

        if (id != null && status != null && user != null){
            return this.problemRepository.findAllByCityIdAndStatusAndReportedBy(id,status, user);
        } else if(id != null && status != null){
            return this.problemRepository.findAllByCityIdAndStatus(id, status);
        } else if (id != null && user == null){
            return this.problemRepository.findAllByCityId(id);
        }else if(status != null && user == null){
            return this.problemRepository.findAllByStatus(status);
        } else if(id != null){
            return this.problemRepository.findAllByCityIdAndReportedBy(id, user);
        }else if(status != null){
            return this.problemRepository.findAllByStatusAndReportedBy(status, user);
        }
        return this.problemRepository.findAllByReportedBy(user);
    }

    @Override
    public byte[] readImageBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
