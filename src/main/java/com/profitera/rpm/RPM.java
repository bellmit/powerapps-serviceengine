/*
 * $Id: RPM.java,v 1.82 2004-02-20 08:28:41 jamison Exp $
 */
package com.profitera.rpm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version CVS $Revision: 1.82 $ $Date: 2004-02-20 08:28:41 $
 */
public class RPM {
	public static final String ACCOUNT_QUERY_FILE = "accountquery";
	public static final String CUSTOMER_QUERY_FILE = "customerquery";
	public static final String TREATMENT_QUERY_FILE = "treatmentquery";
	public static final String RULE_PROPERTIES_NAME = "rules";
	final static Log log = LogFactory.getLog(RPM.class);
	public final static int ACCOUNT_WORKLIST_MODE = 1;
	public final static int CUSTOMER_WORKLIST_MODE = 2;	
}

/*
* $Log: not supported by cvs2svn $
* Revision 1.81  2004/02/17 02:39:05  jamison
* Worklist assignment stuff is making some progress
*
* Revision 1.80  2004/02/12 17:08:15  jamison
* Removed useless subclasses that don't do anything but have long names
*
* Revision 1.79  2004/02/12 08:46:37  jamison
* Compile fixes
*
* Revision 1.78  2004/02/11 09:08:05  jamison
* Compile fixes
*
* Revision 1.77  2004/02/09 11:54:15  jamison
* Fairly major changes to accommodate treatment rules being done using deftemplate.
*
* Revision 1.76  2003/12/08 01:40:11  jamison
* Changes to compile only, this code isn't really used anymore
*
* Revision 1.75  2003/12/04 03:45:45  jamison
* Added error message for when None treatment subtype is not found,
* indicating that system ref data is screwed
*
* Revision 1.74  2004/01/13 03:39:01  jamison
* Added RPM log (package leve)
*
* Revision 1.73  2004/01/11 09:11:23  jamison
* JessRule implimentation is now loaded from rules.properties
*
* Revision 1.72  2004/01/11 07:16:34  jamison
* Changed all things RPM! Reduced dependecies (but not eliminated) on
* jess packages and changed rule implementation to depend on an interface
* rather than a specific implementation, I will have dynamic loading of the 
* implementation to use working soon!
*
* Revision 1.71  2003/12/19 15:17:36  jamison
* Post-MoR fix, not tested
*
* Revision 1.70  2003/11/21 08:52:38  jamison
* Treatment assignment separated out into a separate process/class
* RPM.java is still 'sortof' up to date, in that it compiles but I'm
* not sure that it really works
*
* Revision 1.69  2003/11/20 09:14:35  jamison
* Duh. Putting .properties at the ned of my prop file names really messed things up
*
* Revision 1.68  2003/11/19 13:38:20  jamison
* New architecture for descriptors that externalizes SQL and makes temp table-normal quering combos possible with a single code base... not yet implemented that way, but will be
*
* Revision 1.67  2003/11/15 09:50:24  jamison
* *** empty log message ***
*
* Revision 1.66  2003/11/14 05:39:24  jamison
* *** empty log message ***
*
* Revision 1.65  2003/11/14 05:31:57  jamison
* Uses customer prfile instead of straight queries
*
* Revision 1.64  2003/11/13 05:19:41  jamison
* Risk level was not being assigned properly
*
* Revision 1.63  2003/11/11 15:00:55  jamison
* Some reorg and experiments in speeding up the RPM, Alpha stuff, 
* but commited since there is no hope of the original being fast enough anyway
*
* Revision 1.62  2003/11/10 02:53:30  jamison
* Iteration bugs, I'm using while loops from now on for iterators!
*
* Revision 1.61  2003/10/20 11:00:37  jamison
* Fixed treatment plan creation bug. After a plan was creatd it was not kept
* around and null was passed into the process creation, which causes it to bail 
* out, now the new plan is passed in
*
* Revision 1.60  2003/10/15 09:53:25  jamison
* Bug fixes for treatment
*
* Revision 1.59  2003/10/13 12:47:46  jamison
* Refactored to use the same session for everything
*
* Revision 1.58  2003/10/09 07:09:51  jamison
* Exploits the ObjectPool's cache and uses risk ref's min score to assign
* risk level now
*
* Revision 1.57  2003/10/04 07:06:49  jamison
* When the RPM hits an account w/ a special WL it jsut assigns it straight-away
*
* Revision 1.56  2003/10/04 07:04:14  jamison
* Now throws an exception when Account constant is missing right off the bat
*
* Revision 1.55  2003/09/24 03:17:14  jamison
* ObjectPoolManager interface changed, more static
*
* Revision 1.54  2003/09/23 10:48:54  jamison
* Framed up the code for pulling FSL accounts before RPM processing
*
* Revision 1.53  2003/09/22 13:39:53  jamison
* NullPOinterEx fix
*
* Revision 1.52  2003/09/20 10:23:25  jamison
* Changed to refelct the fact the warklist stuff has been moved out of
* the RPM core
*
* Revision 1.51  2003/09/19 11:44:18  jamison
* b/c of changes in RPMDataManager
*
* Revision 1.50  2003/09/18 08:25:39  jamison
* Implemented profiles for customers and non-rule based segment
* assignment
*
* Revision 1.49  2003/09/18 02:53:25  jamison
* Added a bunch of proper logging and moved the customer profile stuff 
* up to static members
*
* Revision 1.48  2003/09/17 12:04:09  jamison
* Customer profile stuff started
*
* Revision 1.47  2003/09/17 06:38:08  jamison
* Some refactoring to allow for upcoming customer profiling changes
*
* Revision 1.46  2003/09/08 02:54:07  jamison
* Imports optimized
*
* Revision 1.45  2003/09/06 11:57:00  jamison
* Worklist gen seems to be OK
*
* Revision 1.44  2003/09/06 04:24:06  jamison
* NONE_WORKLIST attrib added to RPMDataManager
*
* Revision 1.43  2003/09/06 04:10:32  jamison
* RPM is now Customer based
*
* Revision 1.42  2003/09/05 06:12:50  jamison
* Worklist descriptor change related changes
*
* Revision 1.41  2003/09/04 10:20:08  jamison
* Optimized imports to get rid of compile error caused by CRF 41
*
* Revision 1.40  2003/09/03 12:49:49  jamison
* Most of the data manipulation stuff is moved to RPMDataManager.
* Treatments are ALMOST working properly, but require more DB changes
* to get there
*
* Revision 1.39  2003/09/02 04:10:34  jamison
* Signatures of rpm descriptors changed so they use the same db objects
* over, now the RPM can make one big object query at the start of execution
* and use the data everywhere.
*
* Revision 1.38  2003/09/01 09:52:24  jamison
* Changed back to getNextTreatmentActionType
*
* Revision 1.37  2003/09/01 03:42:33  jamison
* Changes triggered by db changes
*
* Revision 1.35  2003/08/27 14:26:35  jamison
* Added a whole bunch of comments, mostly in places that will be 
* completely borken in the near future by changes anyway...
*
* Revision 1.34  2003/08/27 11:48:23  jamison
* Moved most of the logic for running the agents into the 'startProcess' method
* so the logic is more tranparent (and more correct as well).
* Got rid of a bunch of code duplication and created some bugs in the process 
* of generalizing the RPM better (contingent on DB changes).
*
* Revision 1.33  2003/08/23 10:01:06  jamison
* Delq management nolonger saves anything to the DB
*
* Revision 1.32  2003/08/22 09:16:35  jamison
* RPM now generates more specific exceptions for worklist failure
*
* Revision 1.31  2003/08/22 08:29:38  jamison
* THe synchronization stuff for RPM has been simplified now, one public
* method, and it is synchronized, also agents have their own methods 
* since they are quite specialized
*
* Revision 1.30  2003/08/22 04:25:42  jamison
* Removed unused field
*
* Revision 1.29  2003/08/21 09:47:53  jamison
* The age of the aggregate rule agent is over! AggregateRuleAgent and
* all the related stuff was stripped out of the RPM stuff! Long live
* BaseRuleAgent!
*
* Revision 1.28  2003/08/20 04:28:25  jamison
* Agent codes for processing now in RuleAgentConstants.
*
* Revision 1.27  2003/08/19 05:01:34  jamison
* Added support for TreatmentStreamRef instead of String code
*
* Revision 1.26  2003/08/19 03:41:50  jamison
* Fix related to addition of TreatementStreamRef type
*
* Revision 1.25  2003/08/15 09:21:08  jamison
* Organize imports
*
* Revision 1.24  2003/08/15 06:16:11  jamison
* Started changes to clean up treament... this will take a while as
* the requirements are not well understood
*
* Revision 1.23  2003/08/15 01:14:47  jamison
* Got rid of the million getRef functions and replaced with 
* one generic get ref function
*
* Revision 1.22  2003/08/14 01:21:57  jamison
* Compatable with new deliq. desc.
*
* Revision 1.21  2003/08/12 06:50:32  jamison
* isChanged no longer supported in AccountPreprocessor descriptor
*
* Revision 1.20  2003/08/08 10:37:33  jamison
* isChanged was removed from Profile Object
*
* Revision 1.19  2003/08/06 07:52:43  jamison
* Minor whitespace fix for error message.
*
* Revision 1.18  2003/08/05 06:47:10  jamison
* RuleLoader was obselete and unused so it was removed
*
* Revision 1.17  2003/08/05 03:38:48  jamison
* AccountRPM removed, obselete
*
* Revision 1.16  2003/08/05 02:06:07  mark
* Change import statement for notifier specific beans
*
* Revision 1.15  2003/08/04 12:58:02  jamison
* Now properly composes the score for the preprocessor after the
* agent finishes running.
*
* Revision 1.14  2003/08/04 12:33:36  jamison
* Some spiffy error handling, no more 'throws Exception'!
*
* Revision 1.13  2003/08/04 12:11:49  jamison
* Treament process codes put into constants
* TREATMENT_PROCESS_<treament name>
*
* Revision 1.12  2003/08/04 11:54:53  jamison
* Bad rule loading bug fixed (BaseAgents didn't load their rules) and
* separated the inference engines so each agent gets their own.
*
* Revision 1.11  2003/08/04 01:55:09  jamison
* Changes to support new expression-based rule format
*
* Revision 1.10  2003/08/02 05:18:18  jamison
* Temporary Debugging code.
*
* Revision 1.9  2003/08/02 03:53:56  jamison
* New rule based on Expression and Jess rule support, definately alpha stuff.
*
* Revision 1.8  2003/07/31 05:09:25  walter
* optimize imports
*
* Revision 1.7  2003/07/31 03:09:35  walter
* add Template to readonly class
*
* Revision 1.6  2003/07/31 02:57:44  walter
* add TemplateTypeRef as readonly class
*
*/
