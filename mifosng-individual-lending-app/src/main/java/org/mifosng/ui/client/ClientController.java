package org.mifosng.ui.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.mifosng.data.ClientData;
import org.mifosng.data.ClientDataWithAccountsData;
import org.mifosng.data.ClientList;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.LoanAccountData;
import org.mifosng.data.NoteData;
import org.mifosng.data.command.EnrollClientCommand;
import org.mifosng.data.command.NoteCommand;
import org.mifosng.ui.CommonRestOperations;
import org.mifosng.ui.loanproduct.ClientValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class ClientController {
	
	private final CommonRestOperations commonRestOperations;

	@Autowired
	public ClientController(final CommonRestOperations commonRestOperations) {
		this.commonRestOperations = commonRestOperations;
	}
	
    @ExceptionHandler(AccessDeniedException.class)
	public String accessDeniedException() {
		return "unAuthorizedAction";
	}
	
	@ExceptionHandler(ClientValidationException.class)
	public @ResponseBody Collection<ErrorResponse> validationException(ClientValidationException ex, HttpServletResponse response) {
		
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		response.setContentType("application/json");
		
		return ex.getValidationErrors();
	}
	
	private LocalDate parseStringToLocalDate(String eventDate, String dateFieldIdentifier) {
		LocalDate eventLocalDate = null;
		if (StringUtils.isNotBlank(eventDate)) {
			try {
				Locale locale = LocaleContextHolder.getLocale();
				eventLocalDate = DateTimeFormat.forPattern("dd MMMM yyyy").withLocale(locale).parseLocalDate(eventDate.toLowerCase(locale));
			} catch (IllegalArgumentException e) {
				List<ErrorResponse> validationErrors = new ArrayList<ErrorResponse>();
				validationErrors.add(new ErrorResponse("validation.msg.invalid.date.format", dateFieldIdentifier, eventDate));
				throw new ClientValidationException(validationErrors);
			}
		}
		
		return eventLocalDate;
	}
	
	@RequestMapping(consumes="application/json", produces="application/json", value = "/portfolio/client/{clientId}/note/all", method = RequestMethod.GET)
	public @ResponseBody List<NoteData> retrieveClientNotes(@PathVariable("clientId") Long clientId) {
		
		Collection<NoteData> clientNotes = this.commonRestOperations.retrieveClientNotes(clientId);
		
		return new ArrayList<NoteData>(clientNotes);
	}
	
	@RequestMapping(consumes="application/json", produces="application/json", value = "/portfolio/client/{clientId}/note/new", method = RequestMethod.GET)
	public @ResponseBody NoteData retrieveNewNoteDetails(@PathVariable("clientId") Long clientId) {

		NoteData noteData = new NoteData();
		noteData.setClientId(clientId);
		
		return noteData;
	}
	
	@RequestMapping(consumes="application/x-www-form-urlencoded", produces="application/json", value = "/portfolio/client/{clientId}/note/new", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody EntityIdentifier addClientNote(@PathVariable("clientId") Long clientId,
			@RequestParam(value="note", required=false) String note)  {
		
		NoteCommand command = new NoteCommand();
		command.setClientId(clientId);
		command.setNote(note);
		
		return this.commonRestOperations.addNote(command);
	}
	
	@RequestMapping(consumes="application/json", produces="application/json", value = "/portfolio/client/{clientId}/note/{noteId}", method = RequestMethod.GET)
	public @ResponseBody NoteData retrieveExistingNoteDetails(@PathVariable("clientId") Long clientId, @PathVariable("noteId") Long noteId) {

		return this.commonRestOperations.retrieveClientNote(clientId, noteId);
	}
	
	@RequestMapping(consumes="application/x-www-form-urlencoded", produces="application/json", value = "/portfolio/client/{clientId}/note/{noteId}", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody EntityIdentifier updateClientNote(@PathVariable("clientId") Long clientId, @PathVariable("noteId") Long noteId,
			@RequestParam(value="note", required=false) String note)  {
		
		NoteCommand command = new NoteCommand();
		command.setId(noteId);
		command.setClientId(clientId);
		command.setNote(note);
		
		return this.commonRestOperations.updateNote(command);
	}
	
	@RequestMapping(consumes="application/json", produces="application/json", value = "/portfolio/client/all", method = RequestMethod.GET)
	public @ResponseBody ClientList viewAllClients() {

		return new ClientList(this.commonRestOperations.retrieveAllIndividualClients());
	}
	
	@RequestMapping(consumes="application/json", produces="application/json", value = "/portfolio/client/new", method = RequestMethod.GET)
	public @ResponseBody ClientData newIndividualClientDetails() {

		ClientData clientData = this.commonRestOperations.retrieveNewIndividualClient();
		return clientData;
	}
	
	@RequestMapping(consumes="application/x-www-form-urlencoded", produces="application/json", value = "/portfolio/client/new", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody EntityIdentifier createClient(HttpServletRequest request,
			@RequestParam(value="office", required=false) Long officeId,
			@RequestParam(value="firstname", required=false) String firstname,
			@RequestParam(value="lastname", required=false) String lastname,
			@RequestParam(value="fullname", required=false) String fullname,
			@RequestParam(value="joiningDate", required=false) String joiningDate, 
			@RequestParam(value="externalId", required=false) String externalId)  {
		
		
		LocalDate joiningLocalDate = parseStringToLocalDate(joiningDate, "joiningDate");
		
		EnrollClientCommand command = new EnrollClientCommand(firstname, lastname, fullname, officeId, joiningLocalDate);
		return this.commonRestOperations.enrollClient(command);
	}
	
	@RequestMapping(consumes="application/json", produces="application/json", value = "/portfolio/client/{clientId}", method = RequestMethod.GET)
	public @ResponseBody ClientDataWithAccountsData viewClientAccountData(@PathVariable("clientId") Long clientId) {

		return this.commonRestOperations.retrieveClientAccount(clientId);
	}
	
	@RequestMapping(consumes="application/json", produces="application/json", value = "/portfolio/loan/{loanId}", method = RequestMethod.GET)
	public @ResponseBody LoanAccountData viewLoanAccountData(@PathVariable("loanId") Long loanId) {

		return this.commonRestOperations.retrieveLoanAccount(loanId);
	}
	
	@RequestMapping(value = "/portfolio/client/{clientId}", method = RequestMethod.GET)
	public String viewClientAccount(final Model model, @PathVariable("clientId") final Long clientId) {
		
		ClientData clientData = this.commonRestOperations.retrieveClientDetails(clientId);
		
		model.addAttribute("clientId", clientId);
		model.addAttribute("clientDisplayName", clientData.getDisplayName());
		
		return "client/viewClientAccount";
	}

}