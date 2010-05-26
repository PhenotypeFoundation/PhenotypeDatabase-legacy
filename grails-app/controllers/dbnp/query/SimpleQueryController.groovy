/**
 * SimpleQueryController Controler
 *
 * Description of my controller
 *
 * @author  vincent@ludden.nl
 * @since	20100526
 * @package	dbnp.query
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.query

class SimpleQueryController {
	/**
	 * index closure
	 */
    def index = {
      redirect( action:'pages')
    }

    def pagesFlow = {

        onStart {
            println "Starting webflow simpleQuery"
            flow.page = 0
			flow.pages = [
                [title: 'Query'],
				[title: 'Results']
			]
	    }

        simpleQuery {
          render(view: "/simpleQuery/mainPage")
          onRender {
              println "Rendering simpleQuery interface"
          }

          on ("submit") {
                println "Submitted query string"
	      } .to 'results'
        }

        results {
          render(view: "simpleQuery/results")
          onRender {
              println "Rendering results"
          }
        }
    }

}