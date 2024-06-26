package mk.ukim.finki.localfix.web;

import mk.ukim.finki.localfix.model.*;
import mk.ukim.finki.localfix.model.enums.Impact;
import mk.ukim.finki.localfix.model.enums.Role;
import mk.ukim.finki.localfix.model.enums.Status;
import mk.ukim.finki.localfix.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/***
 * Functionalities:
 * ListProblems - ALL or By User, pagination
 * AddProblem
 * ProblemDetails
 * Filters - Examples(SearchName, City, Country, ProblemType, Institution, Impact, Status)
 * EditProblem - OnlyAdmin
 */
@Controller
public class ProblemController {

    private final ProblemService problemService;
    private final InstitutionService institutionService;
    private final UserService userService;
    private final CityService cityService;
    private final ProblemAdministratorService problemAdministratorService;
    private final PersonService personService;


    public ProblemController(ProblemService problemService,
                             InstitutionService institutionService,
                             UserService userService,
                             CityService cityService,
                             ProblemAdministratorService problemAdministratorService,
                             PersonService personService) {

        this.problemService = problemService;
        this.institutionService = institutionService;
        this.userService = userService;
        this.cityService = cityService;
        this.problemAdministratorService = problemAdministratorService;
        this.personService = personService;
    }


    /*ListProblems - ALL or By User, pagination*/
    @GetMapping("/problems")
    public String listProblemsPage(Model model, @RequestParam(required = false) Long cityId,
                                   @RequestParam(required = false) Status status,
                                   HttpServletRequest request){

        List<Problem> problemList = new ArrayList<>();
        List<City> cityList = this.cityService.listAllCities();

        String username = request.getRemoteUser();
        Person loggedPerson = personService.findByUsername(username);
        if(loggedPerson.getRole().equals(Role.ROLE_ADMIN)){
            return "redirect:/problems/administrator";
        }
        User reportedBy = this.userService.findUserByPerson(loggedPerson);

        /*if (cityId == null) {
             //reportedBy = this.userService.findUserByPerson(loggedPerson);

            if (reportedBy != null && status != null) {
                problemList = this.problemService.listAllProblemsByCityIdAndStatus(null,status,reportedBy);
            }else if (reportedBy != null){
                problemList = this.problemService.listAllProblemsByCityIdAndStatus(null,null,reportedBy);
            }
            else {
                // listing all problems published by administrators
                List<Problem_Administrator> problemAdministratorList= 
                        this.problemAdministratorService.listAllProblemAdministrators();
                for (Problem_Administrator p : problemAdministratorList){
                    if (p != null) {
                        Problem problem = this.problemService
                                .findProblemById(p.getProblem().getId());

                        problemList.add(problem);
                    }
                }
            }
        }*/

        /*Filters - Examples(City, Status)*/

        /*else{
            //reportedBy = this.userService.findUserById(1L);
            problemList = this.problemAdministratorService
                    .listAllProblemAdministratorsByCityIdAndStatus(cityId,status,reportedBy)
                    .stream()
                    .map(l -> this.problemService.findProblemById(l.getProblem().getId()))
                    .toList();
        }*/
        problemList = this.problemAdministratorService
                .listAllProblemAdministratorsByCityIdAndStatus(cityId,status,reportedBy)
                .stream()
                .map(l -> this.problemService.findProblemById(l.getProblem().getId()))
                .toList();
        model.addAttribute("person", username);
        model.addAttribute("reportedBy",reportedBy);
        model.addAttribute("problems", problemList);
        model.addAttribute("cities", cityList);
        model.addAttribute("statuses", Status.values());
        return "list-problems";
    }

    /*AddProblem*/
    @GetMapping ("/problem/add")
    public String addProblemPage(Model model, HttpServletRequest request){
        String username = request.getRemoteUser();

        model.addAttribute("person", username);

        model.addAttribute("status",Status.values());

        model.addAttribute("impact", Impact.values());

        model.addAttribute("institutions", this.institutionService.listAllInstitutions());

        model.addAttribute("cities", this.cityService.listAllCities());

        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);

        if (inputFlashMap != null){
            String invalidImageFormat = (String) inputFlashMap.get("invalidImageFormat");
            String fileTooLarge = (String) inputFlashMap.get("fileTooLarge");

            if (invalidImageFormat != null){
                model.addAttribute("invalidImageFormat",invalidImageFormat);
                model.addAttribute("fileTooLarge",fileTooLarge);
            }
        }

        return "add-problem";
    }
    @PostMapping("/problem/add")
    public String createProblem(@RequestParam String title, @RequestParam String description,
                                @RequestParam("image") MultipartFile file, @RequestParam Long institutionId,
                                @RequestParam Impact impact,
                                @RequestParam Long cityId, @RequestParam String address,
                                HttpServletRequest request, RedirectAttributes redirectAttributes) {


        double fileSizeInMegabytes = (double) file.getSize() / (1024 * 1024);

        if (fileSizeInMegabytes < 25) {

            String fileName = file.getOriginalFilename();

            String fileExtension = Optional.ofNullable(fileName)
                    .map(name -> name.substring(name.lastIndexOf(".") + 1).toLowerCase())
                    .orElse(null);

            if (!"jpg".equals(fileExtension) && !"jpeg".equals(fileExtension) && !"png".equals(fileExtension) && !"webp".equals(fileExtension)
                    && !"avif".equals(fileExtension)) {

                redirectAttributes.addFlashAttribute("invalidImageFormat", "Invalid image format");
                return "redirect:/problem/add";
            }

        }
        else {
            redirectAttributes.addFlashAttribute("fileTooLarge", "Uploaded image file size should be less than 25 MB");
            return "redirect:/problem/add";
        }

        byte [] photo = this.problemService.readImageBytes(file);

        String username = request.getRemoteUser();
        Person loggedPerson = personService.findByUsername(username);
        if(loggedPerson.getRole().equals(Role.ROLE_ADMIN)){
            return "redirect:/problems/administrator";
        }
        User reportedBy = this.userService.findUserByPerson(loggedPerson);

        this.problemService.saveProblem(title, description, photo, institutionId,cityId,impact,address, reportedBy.getId());

        return "redirect:/problems";
    }

    /*ProblemDetails*/
    @GetMapping("/problem/detailed/view/{id}")
    public String detailedView(@PathVariable Long id, Model model, HttpServletRequest request){
        String username = request.getRemoteUser();
        model.addAttribute("person", username);

        Problem problem = this.problemService.findProblemById(id);

        User reportedBy = this.userService.findById(problem.getReportedBy().getId());

        byte [] imageData = problem.getPhoto();

        String base64Image = Base64.getEncoder().encodeToString(imageData);

        model.addAttribute("problem", problem);
        model.addAttribute("reportedBy", reportedBy);
        model.addAttribute("image",base64Image);
        return "detail-view-problem";
    }



    /*** EditProblem - OnlyAdmin
    *  Edit problem mapping is located
     *  in AdministratorController
    * */
    @PostMapping("/problem/add/{id}")
    public String updateProblem(@PathVariable Long id,@RequestParam String title,
                                @RequestParam String address,
                                //@RequestParam(required = false,name = "image") MultipartFile file,
                                @RequestParam String description,
                                @RequestParam Status status,
                                @RequestParam Impact impact,
                                @RequestParam Long institutionId,
                                @RequestParam Long cityId) {


        /*double fileSizeInMegabytes = (double) file.getSize() / (1024 * 1024);

        if (fileSizeInMegabytes < 25) {

            String fileName = file.getOriginalFilename();

            String fileExtension = Optional.ofNullable(fileName)
                    .map(name -> name.substring(name.lastIndexOf(".") + 1).toLowerCase())
                    .orElse(null);

            if (!"jpg".equals(fileExtension) && !"jpeg".equals(fileExtension) && !"png".equals(fileExtension) && !"webp".equals(fileExtension)
                    && !"avif".equals(fileExtension)) {

                redirectAttributes.addFlashAttribute("invalidImageFormat", "Invalid image format");
                return "redirect:/problem/add";
            }

        }
        else {

            redirectAttributes.addFlashAttribute("fileTooLarge", "Uploaded image file size should be less than 25 MB");
            return "redirect:/problem/add";
        }


        byte [] photo = this.problemService.readImageBytes(file);*/

        this.problemService.editProblem(id,title,address,null,description,status,impact,institutionId, cityId);

        return "redirect:/problems";

    }

}
