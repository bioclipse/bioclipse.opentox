package net.bioclipse.opentox.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import net.bioclipse.usermanager.IAccounts;

public class OpenToxAccount implements IAccounts {

	private Text nameTxt;
	private Text pswTxt;
	
	public OpenToxAccount() {	}

	@Override
	public Composite createComposite(Composite container)  {
		int hight = 160;
		int width = 430;
		
		Composite as = new Composite(container, SWT.NONE);
		GridData gd_as = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_as.widthHint = width;
		gd_as.heightHint = hight;
		as.setLayoutData(gd_as);
				
		Label nameLable = new Label(as, SWT.NONE);
		nameLable.setText("User name:");
		nameLable.setBounds(10, 20, 65, 14);
		nameTxt = new Text(as, SWT.BORDER);
		nameTxt.setBounds(80, 17, 200, 20);
		
		Label pswLabel = new Label(as, SWT.NONE);
		pswLabel.setBounds(10, 60, 60, 14);
		pswLabel.setText("Password:");
		pswTxt = new Text(as, SWT.BORDER | SWT.PASSWORD);
		pswTxt.setBounds(80, 57, 200, 20);;
		ImageDescriptor imDesc = ImageDescriptor.createFromFile(this.getClass(), "opentox.jpg");
		Image temp = imDesc.createImage();
		Image image = new Image(container.getDisplay(), temp , SWT.IMAGE_COPY);
		int x = (width-10) - image.getBounds().width;		
		Label im_label = new Label(as, SWT.NONE);
		im_label.setBounds(x, 0, image.getBounds().width, 
				image.getBounds().height);
		im_label.setImage(image);
		System.out.println(im_label.getImage());
		nameTxt.setFocus();
		
		return as;	 
	}

	@Override
	public String getName() {
		return "OpenTox Account";
	}

	@Override
	public void createAccount() {
		System.out.println("Creating a open tox account...");
		System.out.println("Name: " + nameTxt.getText());
		System.out.println("Password: " + pswTxt.getText());
	}
	
	@Override
	public void setFocus() {
		nameTxt.setFocus();
	}

}