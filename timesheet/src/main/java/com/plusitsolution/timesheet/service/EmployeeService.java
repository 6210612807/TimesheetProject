package com.plusitsolution.timesheet.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.plusitsolution.timesheet.domain.Employee.EmpDetailDomain;
import com.plusitsolution.timesheet.domain.Employee.EmployeeDomain;
import com.plusitsolution.timesheet.domain.Timesheet.TimesheetsDomain;
import com.plusitsolution.timesheet.domain.EnumDomain.DateStatus;
import com.plusitsolution.timesheet.domain.EnumDomain.EmpRole;
import com.plusitsolution.timesheet.domain.EnumDomain.MedStatus;
import com.plusitsolution.timesheet.domain.Medical.MedicalDomain;
import com.plusitsolution.timesheet.domain.wrapper.EmployeeIDMonthWrapper;
import com.plusitsolution.timesheet.domain.wrapper.EmployeeIDWrapper;
import com.plusitsolution.timesheet.domain.wrapper.EmployeeLoginWrapper;
import com.plusitsolution.timesheet.domain.wrapper.EmployeeProfileDomain;
import com.plusitsolution.timesheet.domain.wrapper.MedicalRequestWrapper;
import com.plusitsolution.timesheet.domain.wrapper.UpdateMyTimesheetsWrapper;
import com.plusitsolution.timesheet.entity.EmployeeEntity;
import com.plusitsolution.timesheet.entity.MedicalEntity;
import com.plusitsolution.timesheet.repository.EmployeeRepository;
import com.plusitsolution.timesheet.repository.MedicalRepository;
import com.plusitsolution.timesheet.repository.OrganizeRepository;

@Service
@EnableScheduling
public class EmployeeService {
	
	@Autowired
	private OrganizeRepository orgRepository;
	@Autowired
	private EmployeeRepository employeeRepository;
	@Autowired
	private MedicalRepository medicalRepository ;
	@Autowired
	private UtilsService utilService;
	
	public EmployeeProfileDomain loginEmp(EmployeeLoginWrapper wrapper) {
		
		EmployeeEntity employeeEntity = employeeRepository.findByUsername(wrapper.getUsername());
		
		if (employeeEntity == null ) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "this username not have ");
		}
		Map<String , EmpDetailDomain> EMP_MAP = orgRepository.findById(employeeEntity.getOrgID()).get().getEMP_MAP();
		
		EmployeeProfileDomain domain = new EmployeeProfileDomain(employeeEntity.getEmpID(), employeeEntity.getOrgID(), employeeEntity.getEmpCode(), employeeEntity.getFirstName(),
				employeeEntity.getLastName(), employeeEntity.getNickName(), EMP_MAP.get(employeeEntity.getEmpID()).getHolidayID(), EMP_MAP.get(employeeEntity.getEmpID()).getLeaveLimit(),
				EMP_MAP.get(employeeEntity.getEmpID()).getMedFeeLimit(), EMP_MAP.get(employeeEntity.getEmpID()).getEmpRole(), EMP_MAP.get(employeeEntity.getEmpID()).getEndContract(),
				myLeaveDayThisYear(employeeEntity.getEmpID(), LocalDate.now().getYear()), myMedfeeThisYear(employeeEntity.getEmpID(), LocalDate.now().getYear()),
				EMP_MAP.get(employeeEntity.getEmpID()).getLeaveLimit()-myLeaveDayThisYear(employeeEntity.getEmpID(), LocalDate.now().getYear()),
				EMP_MAP.get(employeeEntity.getEmpID()).getMedFeeLimit()-myMedfeeThisYear(employeeEntity.getEmpID(), LocalDate.now().getYear()), 
				orgRepository.findById(employeeEntity.getOrgID()).get().getOrgNameEng(), orgRepository.findById(employeeEntity.getOrgID()).get().getOrgNameTh());
		
		
		return domain;
	}
	
	public EmployeeProfileDomain getUserProfile(EmployeeIDWrapper wrapper) {
		
//		String empID, String orgID, String empCode, String firstName, String lastName,
//		String nickName, String holidayID, double leaveLimit, double medFeeLimit,
//		EmpRole empRole, String endContract, Double leaveUse, Double medFeeUse, Double leaveRemain,
//		Double medFeeRemain
		
		EmployeeEntity employeeEntity = employeeRepository.findById(wrapper.getEmpID()).get();
		if (employeeEntity == null ) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "this employee is't exist"); 
		}
		
		Map<String , EmpDetailDomain> EMP_MAP = orgRepository.findById(employeeEntity.getOrgID()).get().getEMP_MAP();
		
		EmployeeProfileDomain domain = new EmployeeProfileDomain(employeeEntity.getEmpID(), employeeEntity.getOrgID(), employeeEntity.getEmpCode(), employeeEntity.getFirstName(),
				employeeEntity.getLastName(), employeeEntity.getNickName(), EMP_MAP.get(employeeEntity.getEmpID()).getHolidayID(), EMP_MAP.get(employeeEntity.getEmpID()).getLeaveLimit(),
				EMP_MAP.get(employeeEntity.getEmpID()).getMedFeeLimit(), EMP_MAP.get(employeeEntity.getEmpID()).getEmpRole(), EMP_MAP.get(employeeEntity.getEmpID()).getEndContract(),
				myLeaveDayThisYear(employeeEntity.getEmpID(), LocalDate.now().getYear()), myMedfeeThisYear(employeeEntity.getEmpID(), LocalDate.now().getYear()),
				EMP_MAP.get(employeeEntity.getEmpID()).getLeaveLimit()-myLeaveDayThisYear(employeeEntity.getEmpID(), LocalDate.now().getYear()),
				EMP_MAP.get(employeeEntity.getEmpID()).getMedFeeLimit()-myMedfeeThisYear(employeeEntity.getEmpID(), LocalDate.now().getYear()), 
				orgRepository.findById(employeeEntity.getOrgID()).get().getOrgNameEng(), orgRepository.findById(employeeEntity.getOrgID()).get().getOrgNameTh());

		
		return domain;
	}
	
	public Map<String, TimesheetsDomain> getMyTimesheetMonth(EmployeeIDMonthWrapper wrapper) {
		EmployeeEntity employeeEntity = employeeRepository.findById(wrapper.getEmpID()).get();
		if (employeeEntity == null ) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "this employee is't exist"); 
		}
		
		Map<String, TimesheetsDomain> MYTIMESHEETS_MAP = new HashMap<>();
		
		for (String i : employeeEntity.getTIMESHEETS_MAP().keySet()) {
			LocalDate myObj = LocalDate.parse(i);
			if (myObj.getMonthValue() == wrapper.getMonth() && myObj.getYear() == wrapper.getYear()){
				
				TimesheetsDomain timesheetsDomain = employeeEntity.getTIMESHEETS_MAP().get(i)	;	
				MYTIMESHEETS_MAP.put(i, timesheetsDomain);	
			}
		}
		return MYTIMESHEETS_MAP;
	}
	
	public Map<String, MedicalEntity> geMyMedRequests(EmployeeIDWrapper wrapper) {
		EmployeeEntity employeeEntity = employeeRepository.findById(wrapper.getEmpID()).get();
		if (employeeEntity == null ) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "this employee is't exist"); 
		}
		Map<String, MedicalEntity> MYMEDFEE_MAP = new HashMap<>();
		for (String i : employeeEntity.getMEDFEEUSE_MAP().keySet()) {
			String medID = employeeEntity.getMEDFEEUSE_MAP().get(i);
			
			MedicalEntity medicalEntity = medicalRepository.findById(medID).get();
			MYMEDFEE_MAP.put(i, medicalEntity);
		}
		
		return MYMEDFEE_MAP;
	}
	
	//-------------- Medical
	
	public void addMedRequests(MedicalRequestWrapper wrapper) {
		
		EmployeeEntity employeeEntity = employeeRepository.findByEmpCode(wrapper.getEmpCode());
		if (employeeEntity == null ) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "this employee is't exist"); 
		}
		
		MedicalDomain medicalDomain = new MedicalDomain(employeeEntity.getEmpID() , employeeEntity.getOrgID() , wrapper.getSlipPic() , wrapper.getAmount() , 
				wrapper.getNote() , LocalDate.now().toString() , MedStatus.INPROCESS );
		
		MedicalEntity medicalEntity = medicalRepository.save(medicalDomain.toEntity()) ;
		
		employeeEntity.getMEDFEEUSE_MAP().put(LocalDateTime.now().toString(), medicalEntity.getMedID());
		employeeRepository.save(employeeEntity);
		
	}
	
	//------------ Timesheet
	
	public void updateMyTimesheets(UpdateMyTimesheetsWrapper wrapper) {
        EmployeeEntity employeeEntity = employeeRepository.findById(wrapper.getEmpID()).get();
        if (employeeEntity == null ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "this employee is't exist"); 
        }
        
        for (String i : wrapper.getTIMESHEETS_MAP().keySet()) {
        	 TimesheetsDomain domain = wrapper.getTIMESHEETS_MAP().get(i);
        	if(wrapper.getTIMESHEETS_MAP().get(i).getDateStatus().equals(DateStatus.RECORD)) {
        		domain.setDateStatus(checkdateStatus(domain.getTimeIn(), domain.getTimeOut()));
        	}
        }
        
        employeeEntity.getTIMESHEETS_MAP().putAll(wrapper.getTIMESHEETS_MAP());
        employeeRepository.save(employeeEntity);
    }
	
	public DateStatus checkdateStatus(String timein, String timeout) {
		
		if( LocalTime.parse(timeout).getHour() - LocalTime.parse(timein).getHour() == 8) {
			return DateStatus.WORK;
		} else if(LocalTime.parse(timeout).getHour() - LocalTime.parse(timein).getHour() < 8 && LocalTime.parse(timeout).getHour() - LocalTime.parse(timein).getHour() > 4) {
			return DateStatus.HALFDAY;
		} else if(LocalTime.parse(timeout).getHour() - LocalTime.parse(timein).getHour() > 8) {
			return DateStatus.OT;
		} else 
			return DateStatus.LEAVE;

	}
	
	public Double myLeaveDayThisMonth(String empID, int month, int year) {
		
		Map<String , TimesheetsDomain> TIMESHEETS_MAP = new HashMap<>();
		TIMESHEETS_MAP.putAll(employeeRepository.findById(empID).get().getTIMESHEETS_MAP()); 
		
		Double totalLeaveThisMonth = 0.0;
		for (String i : TIMESHEETS_MAP.keySet()) {
			LocalDate date = LocalDate.parse(i);
			if(date.getYear() == year && date.getMonthValue() == month) {
				if(TIMESHEETS_MAP.get(i).getDateStatus().equals(DateStatus.LEAVE)) {
					totalLeaveThisMonth += 1;
				}
			}
		}
		
		return totalLeaveThisMonth;
	}
	
	public Double myLeaveDayThisYear(String empID, int year) {
		
		Double totalLeave = 0.0;
		for(int i=1; i<13; i++) {
			totalLeave += myLeaveDayThisMonth(empID, i, year);
		}
		
		return totalLeave;
	}
	
	public Double myMedfeeThisMonth(String empID, int month, int year) {
		
		Map<String , String> MEDFEEUSE_MAP = new HashMap<>();
		MEDFEEUSE_MAP.putAll(employeeRepository.findById(empID).get().getMEDFEEUSE_MAP()); 
		
		Double totalMedfeeThisMonth = 0.00;
		for (String i : MEDFEEUSE_MAP.keySet()) {
			LocalDateTime date = LocalDateTime.parse(i);
			if(date.getYear() == year && date.getMonthValue() == month) {
				totalMedfeeThisMonth += medicalRepository.findById(MEDFEEUSE_MAP.get(i)).get().getAmount();
			}
		}
		
		return totalMedfeeThisMonth;
	}
	
	public Double myMedfeeThisYear(String empID, int year) {
		
		Double totalMedfee = 0.00;
		for(int i=1; i<13; i++) {
			totalMedfee += myMedfeeThisMonth(empID, i, year);
		}
		
		return totalMedfee;
	}
	
}