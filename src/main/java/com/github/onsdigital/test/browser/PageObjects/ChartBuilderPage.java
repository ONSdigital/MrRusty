package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObject;
import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class ChartBuilderPage extends PageObject {

//    <input type="text" id="chart-title" placeholder="[Title]" value="">
//    <input type="text" id="chart-subtitle" placeholder="[Subtitle]" value="">
//    <input type="text" id="chart-source" placeholder="[Source]" value="">
//    <input type="text" id="chart-unit" class="refresh-chart" placeholder="[Unit]" value="">
//    <textarea id="chart-data" class="refresh-chart" placeholder="Paste your data here" rows="4" cols="120"></textarea>
//    <textarea id="chart-alt-text" class="refresh-text" placeholder="[Alt text]"></textarea>
//    <select id="chart-type" class="refresh-chart">
//    <option value="bar">Bar Chart</option>
//    <option value="rotated">Bar Chart (rotated)</option>
//    <option value="line">Line Chart</option>
//    <option value="area">Area Chart</option>
//    <option value="barline">Bar + Line Chart</option>
//    <option value="rotated-barline">Bar + Line Chart (rotated)</option>
//    <option value="dual-axis">Dual Axis</option>
//    <option value="scatter">Scatter Plot Chart</option>
//    <option value="pie">Pie Chart</option>
//    <option value="population">Population Pyramid</option>
//    <option value="confidence-interval">Confidence Interval</option>
//    <option value="rotated-confidence-interval">Confidence Interval (rotated)</option>
//    <option value="box-and-whisker">Box and Whisker</option>
//    <select id="aspect-ratio" class="refresh-chart">
//    <option value="0.56">16:9</option>
//    <option value="0.75">4:3</option>
//    <option value="0.42">21:9</option>
//    <option value="1">1:1</option>
//    <option value="1.3">10:13</option>
//    <input type="text" id="chart-label-interval" class="refresh-chart" placeholder="[Label interval]" value="">
//    <input type="text" id="chart-decimal-places" class="refresh-chart" placeholder="[Decimal places]" value="">
//    <input type="text" id="chart-x-axis-label" style="width: 99.7%" class="refresh-chart" placeholder="[X axis label]" value="">
//    <textarea id="chart-notes" class="refresh-text" placeholder="Add chart notes here" rows="4" cols="120"></textarea>

    By chartNameEntryLocator = By.id("chart-title");
    By chartSubtitleEntryLocator = By.id("chart-subtitle");
    By chartSourceEntryLocator = By.id("chart-source");
    By chartUnitEntryLocator = By.id("chart-unit");
    By chartDataEntryLocator = By.id("chart-data");
    By chartAltTextEntryLocator = By.id("chart-alt-text");
    By chartTypeEntryLocator = By.id("chart-type");
    By chartAspectRatioEntryLocator = By.id("aspect-ratio");
    By chartLabelIntervalEntryLocator = By.id("chart-label-interval");
    By chartDecPlacesEntryLocator = By.id("chart-decimal-places");
    By chartXAxisEntryLocator = By.id("chart-x-axis-label");
    By chartNotesEntryLocator = By.id("chart-notes");
    By fileEntryLocator = By.id("files");
    By submitButtonLocator = By.id("files");
    By createButtonLocator = By.className("btn-chart-builder-create");
    By cancelButtonLocator = By.className("btn-chart-builder-cancel");

    WebElement chartNameEntry;
    WebElement chartSubtitleEntry;
    WebElement chartSourceEntry;
    WebElement chartUnitEntry;
    WebElement chartDataEntry;
    WebElement chartAltTextEntry;
    Select chartTypeEntry;
    Select chartAspectRatioEntry;
    WebElement chartLabelIntervalEntry;
    WebElement chartDecPlacesEntry;
    WebElement chartXAxisEntry;
    WebElement chartNotesEntry;
    WebElement fileEntry;
    WebElement submitButton;
    WebElement createButton;
    WebElement cancelButton;

    /**
     * Check the expected elements are located in the page.
     */
    protected void initialisePage() {
        try {
            cancelButton = waitAndFind(cancelButtonLocator);
            createButton = find(createButtonLocator);
            submitButton = find(submitButtonLocator);
            fileEntry = find(fileEntryLocator);
            chartNameEntry = find(chartNameEntryLocator);
            chartSubtitleEntry = find(chartSubtitleEntryLocator);
            chartSourceEntry = find(chartSourceEntryLocator);
            chartUnitEntry = find(chartUnitEntryLocator);
            chartDataEntry = find(chartDataEntryLocator);
            chartAltTextEntry = find(chartAltTextEntryLocator);
            chartTypeEntry = new Select(find(chartTypeEntryLocator));
            chartAspectRatioEntry = new Select(find(chartAspectRatioEntryLocator));
            chartLabelIntervalEntry = find(chartLabelIntervalEntryLocator);
            chartDecPlacesEntry = find(chartDecPlacesEntryLocator);
            chartXAxisEntry = find(chartXAxisEntryLocator);
            chartNotesEntry = find(chartNotesEntryLocator);
        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the " + this.getClass().getSimpleName() + " contents.", exception);
        }
    }

    /**
     * Constructor for a previously instantiated WebDriver object.
     *
     * @param driver
     */
    public ChartBuilderPage(WebDriver driver) {
        super(driver);
        initialisePage();
    }

    public MarkdownEditorPage clickCancel() {
        cancelButton.click();
        return new MarkdownEditorPage(driver);
    }

    public MarkdownEditorPage clickCreate() {
        createButton.click();
        return new MarkdownEditorPage(driver);
    }

    public ChartBuilderPage typeChartName(String chartName) {
        chartNameEntry.sendKeys(chartName);
        return this;
    }

    public ChartBuilderPage typeSubtitleName(String chartSubtitle) {
        chartSubtitleEntry.sendKeys(chartSubtitle);
        return this;
    }

    public ChartBuilderPage typeSourceName(String chartSource) {
        chartSourceEntry.sendKeys(chartSource);
        return this;
    }

    public ChartBuilderPage typeUnitName(String chartUnit) {
        chartUnitEntry.sendKeys(chartUnit);
        return this;
    }

    public ChartBuilderPage typeDataName(String chartData) {
        chartDataEntry.sendKeys(chartData);
        return this;
    }

    public ChartBuilderPage typeAltTextName(String chartAltText) {
        chartAltTextEntry.sendKeys(chartAltText);
        return this;
    }

    public ChartBuilderPage selectType(String chartType) {
        chartTypeEntry.selectByValue(chartType);
        return this;
    }

    public ChartBuilderPage selectAspectRatio(String chartAspectRatio) {
        chartAspectRatioEntry.selectByValue(chartAspectRatio);
        return this;
    }

    public ChartBuilderPage typeLabelIntervalName(String chartLabelInt) {
        chartLabelIntervalEntry.sendKeys(chartLabelInt);
        return this;
    }

    public ChartBuilderPage typeDecPlacesName(String chartDecPlaces) {
        chartDecPlacesEntry.sendKeys(chartDecPlaces);
        return this;
    }

    public ChartBuilderPage typeXAxisName(String chartXAxis) {
        chartXAxisEntry.sendKeys(chartXAxis);
        return this;
    }

    public ChartBuilderPage typeNotesName(String chartNotes) {
        chartNotesEntry.sendKeys(chartNotes);
        return this;
    }

    public ChartBuilderPage set(String chartName) {
        chartNameEntry.sendKeys(chartName);
        return this;
    }

    public ChartBuilderPage fillInChart() {
        ChartBuilderPage chart = new ChartBuilderPage(driver);
        chart.typeChartName("Test chart");
        chart.typeSubtitleName("Test chart subtitle");
        chart.typeSourceName("ONS test data");
        chart.typeUnitName("Some testing unit");
        chart.typeDataName("date\tJuice\tTravel\n" +
                "2000-01-01\t106.3\t49.843099\n" +
                "2000-02-01\t106.0\t49.931931\n" +
                "2000-03-01\t105.4\t61.478163\n" +
                "2000-04-01\t101.8\t58.981617\n" +
                "2000-05-01\t95.9\t61.223861\n" +
                "2000-06-01\t94.1\t65.601574\n" +
                "2000-07-01\t102.0\t67.89832");
        chart.typeAltTextName("Chart description for accesibility");
        chart.selectType("line");
        chart.selectAspectRatio("0.56");
        chart.typeLabelIntervalName("2");
        chart.typeDecPlacesName("2");
        chart.typeXAxisName("X axis label");
        chart.typeNotesName("Notes go here, now testing");

        return new ChartBuilderPage(driver);
    }

}
