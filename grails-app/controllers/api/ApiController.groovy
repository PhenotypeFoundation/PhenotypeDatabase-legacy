/**
 * Api Controler
 *
 * API for third party applications to interact
 * with GSCF
 *
 * @author  your email (+name?)
 * @since	20120328ma
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package api

import grails.plugins.springsecurity.Secured
import grails.converters.JSON
import dbnp.studycapturing.Study
import dbnp.authentication.SecUser
import org.dbnp.gdt.TemplateFieldType

class ApiController {
    def authenticationService
    def ApiService

	/**
	 * index closure
	 */
    def index = {
        render(view:'index')
    }

    @Secured(['ROLE_CLIENT', 'ROLE_ADMIN'])
    def authenticate = {
        println "api::authenticate: ${params}"

        // see if we already have a token on file for this device id
        def token = Token.findByDeviceID(params.deviceID)

        // generate a new token if we don't have a token on file
        def result = [:]
        try {
            // TODO - check if token belongs to current user?
            if (!token) {
                // generate a token for this device
                token = new Token(
                        deviceID    : params.deviceID,
                        deviceToken : UUID.randomUUID().toString(),
                        user        : authenticationService.getLoggedInUser(),
                        sequence    : 0
                ).save(failOnError: true)
            }

            result = ['token':token.deviceToken,'sequence':token.sequence]

            // set output headers
            response.status = 200
        } catch (Exception e) {
            // caught an error
            response.status = 500
            result = ['error':e.getMessage()]
        }

        response.contentType = 'application/json;charset=UTF-8'

        if (params.containsKey('callback')) {
            render "${params.callback}(${result as JSON})"
        } else {
            render result as JSON
        }
    }

//    @Secured(['ROLE_CLIENT', 'ROLE_ADMIN'])
    def getStudies = {
        println "api::getStudies: ${params}"

        String deviceID = (params.containsKey('deviceID')) ? params.deviceID : ''
        String validation = (params.containsKey('validation')) ? params.validation : ''

        // check
        if (!apiService.validateRequest(deviceID,validation)) {
            response.sendError(401, 'Unauthorized')
        } else {
//            def user = authenticationService.getLoggedInUser()
            def user = Token.findByDeviceID(deviceID).user
            def readableStudies = Study.giveReadableStudies(user)
            def studies = []
            
            // iterate through studies and define resultset
            readableStudies.each { study ->
                // get result data
                studies[ studies.size() ] = [
                        'token'                 : study.getToken(),
                        'title'                 : study.title,
                        'description'           : study.description,
                        'subjects'              : study.subjects.size(),
                        'species'               : study.subjects.species.collect { it.name }.unique(),
                        'assays'                : study.assays.collect { it.module.name }.unique(),
                        'events'                : study.events.size(),
                        'uniqueEvents'          : study.events.collect { it.toString() }.unique(),
                        'samplingEvents'        : study.samplingEvents.size(),
                        'uniqueSamplingEvents'  : study.samplingEvents.collect { it.toString() }.unique(),
                        'eventGroups'           : study.eventGroups.size(),
                        'uniqueEventGroups'     : study.eventGroups.collect { it.name }.unique(),
                        'samples'               : study.samples.size()
                ]
            }
            

            def result = [
                    'count'     : studies.size(),
                    'studies'   : studies
            ]

            // set output headers
            response.status = 200
            response.contentType = 'application/json;charset=UTF-8'

            if (params.containsKey('callback')) {
                render "${params.callback}(${result as JSON})"
            } else {
                render result as JSON
            }
        }
    }

//    @Secured(['ROLE_CLIENT', 'ROLE_ADMIN'])
    def getSubjectsForStudy = {
        println "api::getSubjectsForStudy: ${params}"

        String deviceID     = (params.containsKey('deviceID')) ? params.deviceID : ''
        String validation   = (params.containsKey('validation')) ? params.validation : ''
        String studyToken   = (params.containsKey('studyToken')) ? params.studyToken : ''

        // fetch user and study
//        def user    = authenticationService.getLoggedInUser()
        def user    = Token.findByDeviceID(deviceID).user
        def study   = Study.findByStudyUUID(studyToken)
        
        // check
        if (!apiService.validateRequest(deviceID,validation)) {
            response.sendError(401, 'Unauthorized')
        } else if (!study) {
            response.sendError(400, 'No such study')
        } else if (!study.canRead(user)) {
            response.sendError(401, 'Unauthorized')
        } else {
            def subjects = []
            
            // iterate through subjects
            study.subjects.each {
                def fields  = it.giveFields()
                def subject = [:]

                // add subject id
                subject['id'] = it.id

                // add subject field values
                fields.each { field ->
                    def value = it.getFieldValue( field.name )

                    if (value.hasProperty('name')) {
                        subject[ field.name ] = value.name
                    } else {
                        subject[ field.name ] = value
                    }
                }

                subjects[ subjects.size() ] = subject
            }
            
            // define result
            def result = [
                    'count'     : study.subjects.size(),
                    'subjects'  : subjects
            ]

            // set output headers
            response.status = 200
            response.contentType = 'application/json;charset=UTF-8'

            if (params.containsKey('callback')) {
                render "${params.callback}(${result as JSON})"
            } else {
                render result as JSON
            }
        }
    }


//    @Secured(['ROLE_CLIENT', 'ROLE_ADMIN'])
    def getAssaysForStudy = {
        println "api::getAssaysForStudy: ${params}"

        String deviceID     = (params.containsKey('deviceID')) ? params.deviceID : ''
        String validation   = (params.containsKey('validation')) ? params.validation : ''
        String studyToken   = (params.containsKey('studyToken')) ? params.studyToken : ''

        // fetch user and study
//        def user    = authenticationService.getLoggedInUser()
        def user    = Token.findByDeviceID(deviceID).user
        def study   = Study.findByStudyUUID(studyToken)

        // check
        if (!apiService.validateRequest(deviceID,validation)) {
            println "1"
            response.sendError(401, 'Unauthorized')
        } else if (!study) {
            println "2"
            response.sendError(400, 'No such study')
        } else if (!study.canRead(user)) {
            println "3"
            response.sendError(401, 'Unauthorized')
        } else {
            // define result
            def result = [
//                    'count'     : study.subjects.size(),
//                    'subjects'  : subjects
            ]

            // set output headers
            response.status = 200
            response.contentType = 'application/json;charset=UTF-8'

            if (params.containsKey('callback')) {
                render "${params.callback}(${result as JSON})"
            } else {
                render result as JSON
            }
        }
    }
}