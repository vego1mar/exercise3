package wdsr.exercise3.hr;

import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service.Mode;

import wdsr.exercise3.ws.EmployeeType;
import wdsr.exercise3.ws.HolidayRequest;
import wdsr.exercise3.ws.HolidayResponse;
import wdsr.exercise3.ws.HolidayType;
import wdsr.exercise3.ws.HumanResourceService;

// TODO Complete this class to book holidays by issuing a request to Human Resource web service.
// In order to see definition of the Human Resource web service:
// 1. Run HolidayServerApp.
// 2. Go to http://localhost:8090/holidayService/?wsdl
public class HolidayClient {

    private HumanResourceService service;

    /**
     * Creates this object
     * 
     * @param wsdlLocation URL of the Human Resource web service WSDL
     */
    public HolidayClient(URL wsdlLocation) {
        service = new HumanResourceService(wsdlLocation);
    }

    /**
     * Sends a holiday request to the HumanResourceService.
     * 
     * @param employeeId Employee ID
     * @param firstName First name of employee
     * @param lastName Last name of employee
     * @param startDate First day of the requested holiday
     * @param endDate Last day of the requested holiday
     * @return Identifier of the request, if accepted.
     * @throws ProcessingException if request processing fails.
     */
    public int bookHoliday(int employeeId, String firstName, String lastName, Date startDate, Date endDate) throws ProcessingException {
        EmployeeType employee = getBookingEmployeeType(employeeId, firstName, lastName);
        HolidayType holiday = getBookingHolidayType(startDate, endDate);
        HolidayRequest request = getBookingHolidayRequest(employee, holiday);
        Dispatch<HolidayRequest> dispatch = service.createDispatch(service.getServiceName(), HolidayRequest.class, Mode.MESSAGE);
        HolidayRequest requestMessage = dispatch.invoke(request);
        HolidayResponse responseMessage = new HolidayResponse();
        return responseMessage.getRequestId();
    }

    private EmployeeType getBookingEmployeeType(int id, String firstName, String lastName) {
        EmployeeType employee = new EmployeeType();
        employee.setNumber(id);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        return employee;
    }

    private HolidayType getBookingHolidayType(Date startDate, Date endDate) {
        HolidayType holiday = new HolidayType();
        holiday.setStartDate(getGregorianCalendar(startDate));
        holiday.setEndDate(getGregorianCalendar(endDate));
        return holiday;
    }

    private XMLGregorianCalendar getGregorianCalendar(Date date) {
        XMLGregorianCalendar calendar = null;

        try {
            GregorianCalendar gregory = new GregorianCalendar();
            gregory.setTime(date);
            calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregory);
        } catch (DatatypeConfigurationException x) {
            java.util.logging.Logger.getAnonymousLogger().log(Level.WARNING, x.getMessage());
        }

        return calendar;
    }

    private HolidayRequest getBookingHolidayRequest(EmployeeType employee, HolidayType holiday) {
        HolidayRequest request = new HolidayRequest();
        request.setEmployee(employee);
        request.setHoliday(holiday);
        return request;
    }

}
