package org.mifosplatform.portfolio.savingsdepositaccount.service;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.mifosplatform.infrastructure.core.service.FileUtils;
import org.mifosplatform.portfolio.savingsdepositaccount.data.DepositAccountData;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.CMYKColor;
import com.lowagie.text.pdf.PdfWriter;

public class GeneratePDF {

	private final DepositAccountData account;

	public GeneratePDF(DepositAccountData account) {
		this.account = account;
	}

	public String generatePDF() {

		String fileLocation = FileUtils.MIFOSX_BASE_DIR + File.separator+ "Print_FD_Details";

		/** Recursively create the directory if it does not exist **/
		if (!new File(fileLocation).isDirectory()) {
			new File(fileLocation).mkdirs();
		}
		String printFDdetailsLocation = fileLocation + File.separator + account.getId() + ".pdf";
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMMM-yyyy");
		DecimalFormat decimalFormat = new DecimalFormat("#.##");

		try {
			Document document = new Document();
			PdfWriter.getInstance(document, new FileOutputStream(printFDdetailsLocation));
			document.open();

			Paragraph paragraph = new Paragraph("FIXED DEPOSIT CERTIFICATE ", FontFactory.getFont(FontFactory.COURIER, 14, Font.BOLD, new CMYKColor(0, 255, 0, 0)));

			paragraph.setAlignment(Element.ALIGN_CENTER);
			paragraph.setSpacingAfter(30);

			document.add(paragraph);
			Image img = Image.getInstance(account.getImageKey());
			img.scaleAbsolute(70, 80);
			img.setAbsolutePosition(450, 670);
			document.add(img);
			Font font = FontFactory.getFont(FontFactory.COURIER, 10);
			document.add(new Paragraph("Extenal id: "+ account.getExternalId(), font));
			document.add(new Paragraph("Created date: "+ dateFormat.format(account.getActualCommencementDate().toDate()), font));
			document.add(new Paragraph("Mature date: " + dateFormat.format(account.getMaturedOn().toDate()), font));
			document.add(new Paragraph("Client name: " + account.getClientName(), font));
			document.add(new Paragraph("Amount: " + decimalFormat.format(account.getDeposit()), font));
			document.add(new Paragraph("FD interest rate: " + decimalFormat.format(account.getMaturityInterestRate()), font));
			document.add(new Paragraph("Preclosure interest rate: " + decimalFormat.format(account.getPreClosureInterestRate()), font));
			document.add(new Paragraph("Tenure: " + account.getTenureInMonths(), font));
			document.add(new Paragraph("Maturity Amount: " + decimalFormat.format(account.getActualMaturityAmount()), font));
			document.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return printFDdetailsLocation;
	}

}
