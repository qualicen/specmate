	import './support/gentypes';
	import { Proxy } from './support/proxy';


	export class RobotStep  {

		___nsuri: string = "http://specmate.com/20190125/model/testspecification";
		public url: string;
		public className: string = "RobotStep";
		public static className: string = "RobotStep";
		// Attributes
		public id: EString;
		public name: EString;
		public description: EString;
		public position: EInt;
		public expectedOutcome: EString;

		// References
		public referencedTestParameters: Proxy[];

		// Containment


	}

