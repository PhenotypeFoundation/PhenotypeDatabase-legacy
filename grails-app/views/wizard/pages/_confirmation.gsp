<%
/**
 * Confirmation page
 *
 * @author  Jeroen Wesbeek
 * @since   20100225
 * @package wizard
 * @see     dbnp.studycapturing.WizardTagLib::previousNext
 * @see     dbnp.studycapturing.WizardController
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
%>
<wizard:pageContent>
<span class="info">
	<span class="title">You are almost there...</span>
	You are almost done creating your study. Below you will find a summary of the study you have just defined.
	If everything
	is well, click 'next' to permanently save the study you have created, or click 'previous' if you need to modify parts
	of your study.
</span>

<div id="accordion">
	<h3><a href="#">General overview</a></h3>
	<div>
		<p>
		You are about to create a study containing ${subjects.size()} subjects,
		${eventDescriptions.size()} event descriptions and ${events.size()} events grouped into
		${eventGroups.size()} event groups.
		</p>
	</div>
	<h3><a href="#">Study</a></h3>
	<div>
		<p>
		  <ul>
		<li>title: ${study?.title}</li>
		<li>description: ${study?.description}</li>
		<li>research question: ${study?.researchQuestion}</li>
		<li>code: ${study?.code}</li>
		<li>EC code: ${study?.ecCode}</li>
		<g:each var="field" in="${study.template.fields}">
			<li>${field.name} - ${study.getFieldValue(field.name)}</li>
		</g:each>
		  </ul>
		</p>
		Not right? Click <wizard:ajaxButton name="toStudy" value="here" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" class="prevnext" /> to go back to the study page and make corrections.
	</div>
	<h3><a href="#">Subjects</a></h3>
	<div>
		<p>
		${subjects}
		</p>
		Not right? Click <wizard:ajaxButton name="toSubjects" value="here" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" class="prevnext" /> to go back to the subjects page and make corrections.
	</div>
	<h3><a href="#">Events</a></h3>
	<div>
		<p>
		${subjects}
		</p>
		<ul>
			<li>List item one</li>
			<li>List item two</li>
			<li>List item three</li>
		</ul>
		Not right? Click <wizard:ajaxButton name="toEvents" value="here" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" class="prevnext" /> to go back to the events page and make corrections.
	</div>
</div>

</wizard:pageContent>