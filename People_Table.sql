--Table of the people that need to be davened for
CREATE TABLE People(
    PersonID INT PRIMARY KEY IDENTITY(1,1), --ID number per person
    EntryDate DATETIME DEFAULT GETDATE(), --Date the person was entered
    HebName NVARCHAR(MAX) NOT NULL, --Hebrew Name
    Age INT,
    Bio NVARCHAR(MAX), --A little information about the person
    Shul NVARCHAR(MAX), --What shul the person comes to
    UpTo INT --What Perek of Tehillim needs to be said next
);

--Table of communities that the people may be connected to
CREATE TABLE Communities(
    CommunityID INT PRIMARY KEY IDENTITY(1,1), --ID number per community
    Name NvarChar(MAX) NOT NULL, --Name of community
    Description NVARCHAR(MAX)
);

--Table of what communities people belong to
CREATE TABLE Matchups(
    PersonId INT FOREIGN KEY REFERENCES People(PersonID),
    CommunityID INT FOREIGN KEY REFERENCES Communities(CommunityID)
);