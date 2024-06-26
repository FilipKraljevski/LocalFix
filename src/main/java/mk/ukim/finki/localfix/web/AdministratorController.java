package mk.ukim.finki.localfix.web;

import mk.ukim.finki.localfix.model.*;
import mk.ukim.finki.localfix.model.enums.Impact;
import mk.ukim.finki.localfix.model.enums.Status;
import mk.ukim.finki.localfix.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


@Controller
public class AdministratorController {

    private final ProblemService problemService;
    private final AdministratorService administratorService;
    private final ProblemAdministratorService problemAdministratorService;
    private final InstitutionService institutionService;
    private final PersonService personService;

    private final CityService cityService;

    public AdministratorController(ProblemService problemService,
                                   AdministratorService administratorService,
                                   ProblemAdministratorService problemAdministratorService,
                                   InstitutionService institutionService, PersonService personService,
                                   CityService cityService) {
        this.problemService = problemService;
        this.administratorService = administratorService;
        this.problemAdministratorService = problemAdministratorService;
        this.institutionService = institutionService;
        this.personService = personService;
        this.cityService = cityService;
    }

    /*lists all reported problems by all users */
    @GetMapping("/problems/administrator")
    public String listAllProblemsForAdministrator(Model model,
                                                  @RequestParam(required = false) Long cityId ,
                                                  @RequestParam(required = false) Status status,
                                                    HttpServletRequest request){

        List<Problem> problemList;
//
        String username = request.getRemoteUser();
        model.addAttribute("person", username);
        Person loggedPerson = personService.findByUsername(username);
        Administrator administrator = this.administratorService.findByPerson(loggedPerson);
        model.addAttribute("administrator",administrator);
        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        List<City> cityList = this.cityService.listAllCities();

        if (cityId == null && status == null){
            problemList = this.problemService.listAllProblems();
        }
        else {
            problemList = this.problemService.listAllProblemsByCityIdAndStatus(cityId,status,null);
        }


        if (inputFlashMap != null){

            Long problemNotPublishedId = (Long) inputFlashMap.get("id");
            if (problemNotPublishedId != null){

                String problemNotPublished = (String) inputFlashMap.get("problemNotPublished_" + problemNotPublishedId);

                if (problemNotPublished != null) {
                    model.addAttribute("problemNotPublished", problemNotPublished);
                    model.addAttribute("problemNotPublishedId",problemNotPublishedId);
                }
            }
            Long ProblemAlreadyPublishedId = (Long) inputFlashMap.get("problemId");

            if (ProblemAlreadyPublishedId != null){

                String problemAlreadyPublished = (String) inputFlashMap.get("message");

                if (problemAlreadyPublished != null){

                    model.addAttribute("problemAlreadyPublishedId",ProblemAlreadyPublishedId);
                    model.addAttribute("problemAlreadyPublished",problemAlreadyPublished);
                }
            }
        }

        model.addAttribute("cities",cityList);
        model.addAttribute("problems",problemList);
        model.addAttribute("statuses", Status.values());
        return "list-problems";
    }

    /*publishes an issue from an administrator => creating object in problemAdministrator */
    @PostMapping("/publish/problem/{problemId}")
    public String publishProblem (@PathVariable Long problemId,
                                  RedirectAttributes redirectAttributes, HttpServletRequest request){

        String username = request.getRemoteUser();
        Person loggedPerson = personService.findByUsername(username);
        Administrator administrator = this.administratorService.findByPerson(loggedPerson);

        List<Problem_Administrator> problemAdministratorList = this.problemAdministratorService.listAllProblemAdministrators();

        boolean flag = true;

        if (!problemAdministratorList.isEmpty()){

            for (Problem_Administrator problemAdministrator : problemAdministratorList){
                if (problemAdministrator.getAdministrator().getId().equals(administrator.getId()) &&
                    problemAdministrator.getProblem().getId().equals(problemId)){

                    flag = false;
                    break;
                }

            }
        }
        if (flag){

            Problem problem = this.problemService.findProblemById(problemId);
            Problem_Administrator problemAdministrator =
                    new Problem_Administrator(administrator,problem);
            this.problemAdministratorService.create(problemAdministrator);
        }
        else{
            redirectAttributes.addFlashAttribute("message","problem has already been published");
            redirectAttributes.addFlashAttribute("problemId",problemId);
        }

        return "redirect:/problems/administrator";
    }

    /*gets edit Problem page for editing problem by administrator */
    @GetMapping("/problem/administrator/edit/{id}")
    public String editProblemPage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request){

        String username = request.getRemoteUser();
        model.addAttribute("person", username);
        Person loggedPerson = personService.findByUsername(username);
        Administrator administrator = this.administratorService.findByPerson(loggedPerson);

        List<Problem_Administrator> problemAdministratorList = this.problemAdministratorService.listAllProblemAdministrators();

        boolean flag = false;

        for (Problem_Administrator p : problemAdministratorList){
            if (p.getProblem().getId().equals(id)){
                flag = true;
                break;
            }
        }
        if (flag){
            Problem problem = this.problemService.findProblemById(id);

            model.addAttribute("status", Status.values());
            model.addAttribute("impact", Impact.values());
            model.addAttribute("problem", problem);
            model.addAttribute("institutions", this.institutionService.listAllInstitutions());
            model.addAttribute("administrator",administrator);
            model.addAttribute("cities", cityService.listAllCities());
        }
        else{
            //model.addAttribute("problemNotPublished","publish the problem first before editing");

            redirectAttributes.addFlashAttribute("id", id);
            redirectAttributes.addFlashAttribute("problemNotPublished_" + id, "publish the problem first before editing");

            return "redirect:/problems/administrator";
        }

        return "add-problem";
    }

    /* administrator deletes problem  */
    @PostMapping ("/problem/administrator/delete/{id}")
    public String deleteProblem(@PathVariable Long id){

        this.problemAdministratorService.deleteProblemAdministrator(id);
        return "redirect:/problems/administrator";
    }
}
