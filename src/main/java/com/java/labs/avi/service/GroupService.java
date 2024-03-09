    package com.java.labs.avi.service;

    import com.java.labs.avi.model.Group;
    import com.java.labs.avi.repository.GroupRepository;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;

    import java.util.List;

    @Service
    public class GroupService {
        private final GroupRepository groupRepository;

        @Autowired
        public GroupService(GroupRepository groupRepository) {
            this.groupRepository = groupRepository;
        }

        public List<Group> findAll() {
            return groupRepository.findAll();
        }
    }
