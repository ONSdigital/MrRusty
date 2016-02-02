package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObject;
import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;

/**
 * Created by onsmacpro on 28/01/2016.
 */
public class ImageBuilderPage extends PageObject {

//    <input type="text" id="image-title" placeholder="[Title]" value="">
//    <input type="text" id="image-subtitle" placeholder="[Subtitle]" value="">
//    <input type="text" id="image-source" placeholder="[Source]" value="">
//    <input type="text" id="image-alt-text" placeholder="[Alt text]" value="">
//    <form id="upload-image-form">
//    <input type="file" name="image-upload" id="image-upload">
//    <input type="submit" value="Upload image">
//    <form id="upload-data-form">
//    <input type="file" name="data-upload" id="data-upload">
//    <input type="submit" value="Upload data">
//    <textarea id="image-notes" class="refresh-text" placeholder="Add image notes here" rows="4" cols="120"></textarea>



    By imageNameEntryLocator = By.id("image-title");
    By imageSubtitleEntryLocator = By.id("image-subtitle");
    By imageSourceEntryLocator = By.id("image-source");
    By imageAltTextEntryLocator = By.id("image-alt-text");
    By imageUploadEntryLocator = By.id("image-upload");
    By imageUploadButtonLocator = By.id("upload-image-button");
    By imageDataEntryLocator = By.id("data-upload");
    By imageNotesEntryLocator = By.id("image-notes");
    By createButtonLocator = By.className("btn-image-builder-create");
    By cancelButtonLocator = By.className("btn-image-builder-cancel");

    WebElement imageNameEntry;
    WebElement imageSubtitleEntry;
    WebElement imageSourceEntry;
    WebElement imageAltTextEntry;
    WebElement imageUploadEntry;
    WebElement imageUploadButton;
    WebElement imageDataEntry;
    WebElement imageNotesEntry;
    WebElement createButton;
    WebElement cancelButton;

    /**
     * Check the expected elements are located in the page.
     */
    protected void initialisePage() {
        try {
            cancelButton = waitAndFind(cancelButtonLocator);
            createButton = find(createButtonLocator);
            imageNameEntry = find(imageNameEntryLocator);
            imageSubtitleEntry = find(imageSubtitleEntryLocator);
            imageSourceEntry = find(imageSourceEntryLocator);
            imageDataEntry = find(imageDataEntryLocator);
            imageUploadEntry = find(imageUploadEntryLocator);
            imageUploadButton = find(imageUploadButtonLocator);
            imageAltTextEntry = find(imageAltTextEntryLocator);
            imageNotesEntry = find(imageNotesEntryLocator);
        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the " + this.getClass().getSimpleName() + " contents.", exception);
        }
    }

    /**
     * Constructor for a previously instantiated WebDriver object.
     *
     * @param driver
     */
    public ImageBuilderPage(WebDriver driver) {
        super(driver);
        initialisePage();
    }

    public MarkdownEditorPage clickCancelImageFromMarkdown() {
        cancelButton.click();
        return new MarkdownEditorPage(driver);
    }

    public WorkspacePage clickCancelImageFromMenu() {
        cancelButton.click();
        return new WorkspacePage(driver);
    }

    public MarkdownEditorPage clickCreateImageFromMarkdown() {
        createButton.click();
        return new MarkdownEditorPage(driver);
    }

    public WorkspacePage clickCreateImageFromMenu() {
        createButton.click();
        return new WorkspacePage(driver);
    }

    public ImageBuilderPage typeImageName(String imageName) {
        imageNameEntry.sendKeys(imageName);
        return this;
    }

    public ImageBuilderPage typeSubtitleName(String imageSubtitle) {
        imageSubtitleEntry.sendKeys(imageSubtitle);
        return this;
    }

    public ImageBuilderPage typeSourceName(String imageSource) {
        imageSourceEntry.sendKeys(imageSource);
        return this;
    }

    public ImageBuilderPage typeAltTextName(String imageAltText) {
        imageAltTextEntry.sendKeys(imageAltText);
        return this;
    }

    public void uploadImage() {
        File file = new File("/Users/onsmacpro/git/MrRusty/src/main/resources/snail.jpg");
        String absolutePath = file.getAbsolutePath();
        imageUploadEntry.sendKeys(absolutePath);
        imageUploadButton.click();
    }

    public ImageBuilderPage typeNotesName(String imageNotes) {
        imageNotesEntry.sendKeys(imageNotes);
        return this;
    }

    public ImageBuilderPage fillInImage() {
        ImageBuilderPage image = new ImageBuilderPage(driver);
        image.typeImageName("Test image");
        image.typeSubtitleName("Test image subtitle");
        image.typeSourceName("ONS test data");
        image.uploadImage();
        image.typeAltTextName("Image description for accesibility");
        image.typeNotesName("Notes go here, now testing");

        return new ImageBuilderPage(driver);
    }

}
