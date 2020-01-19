+ Document {
	createBackup {
		var backupDir = this.dir ++ "/backup-SC-files-37";
		var titleSec = this.title.subStr(0, this.getExtensionIndexInTitle - 1);
		var backupFilePath =
			backupDir ++ "/" ++ titleSec ++ "-" ++ Date.getDate.format("%Y%m%d%H%M")++ "." ++ this.getExtension;
		var originalFilePath = this.path;
		("mkdir" + backupDir).unixCmd;
		("cp" + originalFilePath + backupFilePath).unixCmd;
		("Backup saved as" + backupFilePath).postln;
	}
	getExtension {
		^(this.title.subStr(this.getExtensionIndexInTitle + 1))
	}
	getExtensionIndexInTitle {
		^(this.title.findBackwards("."))
	}
}
